package dev.silverest.invoicerback.handlers.utils

import dev.silverest.invoicerback.daos.Mapper
import dev.silverest.invoicerback.daos.Unique
import dev.silverest.invoicerback.repositories.CompanyRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

object HandlerUtils:
  def genericExistenceChecker[A, B, K, RepEnv](repositoryContains: (K, String) => ZIO[RepEnv, Throwable, Boolean], error: HttpError)
                                   (request: Request, userId: String)
                                   (using aUniques: Unique[A, K])
                                   (using aMapper: Mapper[A, B])
                                   (using aDecoder: Decoder[A]) =
    request.bodyAsString
      .map(decode[A]) 
      .flatMap { 
        case Right(a) =>
          repositoryContains(a.extractKeys, userId)
            .map {
              case true => a.mapToModel(userId)
              case false => error
            }

        case Left(e) => ErrorHandler.unprocessableEntity(s"$e")
      }

  def successActionHandler[A, RepEnv](successAction: A => ZIO[RepEnv, Throwable, Long])
                            (a: A)
                            (using aEncoder: Encoder[A])
                            (using aDecoder: Decoder[A]): ZIO[RepEnv, Throwable, Response] =
    for {
      response <- successAction(a) map {l =>
        if l >= 1 then
          Response.json(a.asJson.toString)
        else
          Response.fromHttpError(HttpError.BadRequest("Query failed."))
        }
    } yield response

  def genericGetRequest[A, K, RepEnv](getAction: K => ZIO[RepEnv, Throwable, A])
                                  (key: K)
                                  (using aEncoder: Encoder[A]): ZIO[RepEnv, Throwable, Response] =
    for {
      item <- getAction(key)
    } yield Response.json(item.asJson.toString)

