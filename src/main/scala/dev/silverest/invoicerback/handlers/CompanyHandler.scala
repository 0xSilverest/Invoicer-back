package dev.silverest.invoicerback.handlers

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import dev.silverest.invoicerback.handlers.utils.*
import dev.silverest.invoicerback.models.Company
import dev.silverest.invoicerback.models.Client
import dev.silverest.invoicerback.daos.CompanyDao
import dev.silverest.invoicerback.models.UserJwtDecode
import dev.silverest.invoicerback.models.ICENumber
import dev.silverest.invoicerback.repositories.CompanyRepository
import dev.silverest.invoicerback.services.Authenticator

import pdi.jwt.JwtClaim

import javax.sql.DataSource
import java.util.UUID

class CompanyHandler:
  private type RepEnv = CompanyRepository.Env

  val backendLayer: ZLayer[ZEnv, Nothing, RepEnv] = CompanyRepository.live

  private val authenticator = Authenticator()

  def endpoints(jwtDecoded: UserJwtDecode) =
    val userId = jwtDecoded.username 
    Http.collectZIO[Request] {
      case Method.GET -> _ / "companies" => getAll(userId)

      case Method.GET -> _ / "company" / name => getCompany(userId, name)

      case request @ Method.POST -> _ / "company" => addCompany(userId, request)

      case Method.DELETE -> _ / "company" / name => deleteCompany(userId, name)

      case request @ Method.PUT -> _ / "company" => updateCompany(userId, request)
    }

  def getAll(userId: String) = 
    for {
      companies <- CompanyRepository.getAll(userId)
    } yield Response.json(companies.asJson.toString)

  def getCompany(userId: String, name: String) =
    HandlerUtils.genericGetRequest(CompanyRepository.findByName(userId))(name)

  def addCompany = nonExistenceCheckerPartial(CompanyRepository.insert)

  def updateCompany = existenceCheckerPartial(CompanyRepository.update)

  def deleteCompany(userId: String, name: String) = 
    for {
      _ <- CompanyRepository.delete(userId)(name)
    } yield Response.ok

  private def nonExistenceChecker = 
    HandlerUtils.genericExistenceChecker[CompanyDao, Client.Company, ICENumber, RepEnv](
      CompanyRepository.containsNot,
      HttpError.Conflict("Company already exists")
    )

  private def nonExistenceCheckerPartial(repositoryAction: Client.Company => ZIO[RepEnv, Throwable, Long])
                              (userId: String, request: Request) =
    nonExistenceChecker(request, userId)
      .flatMap {
        case p: Client.Company => HandlerUtils.successActionHandler(repositoryAction)(p)
        case e: HttpError => ErrorHandler.handleError(e)
      }

  private def existenceChecker = 
    HandlerUtils.genericExistenceChecker[CompanyDao, Client.Company, Int, RepEnv](
      CompanyRepository.containsId,
      HttpError.BadRequest("Company doesn't exist")
    )

  private def existenceCheckerPartial(repositoryAction: Client.Company => ZIO[RepEnv, Throwable, Long])
                              (userId: String, request: Request) =
    existenceChecker(request, userId)
      .flatMap {
        case p: Client.Company => HandlerUtils.successActionHandler(repositoryAction)(p)
        case e: HttpError => ZIO.succeed(Response.fromHttpError(e))
      }
