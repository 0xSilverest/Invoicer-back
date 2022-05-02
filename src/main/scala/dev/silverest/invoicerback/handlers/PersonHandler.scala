package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.models.Person
import dev.silverest.invoicerback.models.Client
import dev.silverest.invoicerback.models.UserJwtDecode

import dev.silverest.invoicerback.daos.PersonDao
import dev.silverest.invoicerback.daos.Mapper

import dev.silverest.invoicerback.repositories.PersonRepository
import dev.silverest.invoicerback.handlers.utils.HandlerUtils

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.JwtClaim

class PersonHandler:
  private type RepEnv = PersonRepository.Env

  val backendLayer: ZLayer[ZEnv, Nothing, RepEnv] = PersonRepository.live

  def endpoints (userDecoded: UserJwtDecode) =
    val userId = userDecoded.username
    Http.collectZIO[Request] {
      case Method.GET -> _ / "persons" => getAll(userId)

      case Method.GET -> _ / "person" / firstName / lastName => getPerson(userId, firstName, lastName)

      case request @ Method.POST -> _ / "person"  => insertPerson(userId, request)

      case Method.DELETE -> _ / "person" / id => deletePerson(userId, id.toInt)

      case request @ Method.PUT -> _ / "person"  => updatePerson(userId, request)
    }

  private def getAll(userId: String) = 
    for {
      persons <- PersonRepository.getAll(userId)
    } yield Response.json(persons.asJson.toString)

  private def getPerson(userId: String, firstName: String, lastName: String) =
    HandlerUtils.genericGetRequest(PersonRepository.findByName(userId))(firstName, lastName)

  private def insertPerson = nonExistenceCheckerPartial(PersonRepository.insert)

  private def updatePerson = existenceCheckerPartial(PersonRepository.update)

  private def deletePerson(userId: String, id: Int) =
      for {
        _ <- PersonRepository.delete(id, userId)
      } yield Response.ok

  private def nonExistenceChecker = 
    HandlerUtils.genericExistenceChecker[PersonDao, Client.Person, (String, String), RepEnv](
      PersonRepository.containsNot,
      HttpError.Conflict("Person already exists")
    )

  private def existenceChecker = 
    HandlerUtils.genericExistenceChecker[PersonDao, Client.Person, Int, RepEnv](
      PersonRepository.containsId,
      HttpError.BadRequest("Person doesn't exist")
    )

  private def existenceCheckerPartial(repositoryAction: Client.Person => ZIO[RepEnv, Throwable, Long])
                              (userId: String, request: Request) =
    existenceChecker(request, userId)
      .flatMap {
        case p: Client.Person => HandlerUtils.successActionHandler(repositoryAction)(p)
        case e: HttpError => ZIO.succeed(Response.fromHttpError(e))
      }

  private def nonExistenceCheckerPartial(repositoryAction: Client.Person => ZIO[RepEnv, Throwable, Long])
                              (userId: String, request: Request) =
    nonExistenceChecker(request, userId)
      .flatMap {
        case p: Client.Person => HandlerUtils.successActionHandler(repositoryAction)(p)
        case e: HttpError => ZIO.succeed(Response.fromHttpError(e))
      }
