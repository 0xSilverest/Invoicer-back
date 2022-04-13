package dev.silverest.invoicerback.handlers.utils

import dev.silverest.invoicerback.repositories.CompanyRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

object HandlerUtils:
  def handleEither[A, B, RepEnv](successAction: B => ZIO[RepEnv, Throwable, Long])
                                (using bEncoder: Encoder[B]): Either[Error, B] => ZIO[RepEnv, Throwable, Response] =
    eitherA =>
     eitherA match
      case Right(a) => successAction(a).map {l =>
        if l >= 1 then
          Response.json(a.asJson.toString)
        else
          Response.fromHttpError(HttpError.BadRequest("Query failed."))
        }
      case Left(e) => ZIO.succeed(Response.fromHttpError(HttpError.BadRequest(e.getMessage)))


  def successActionRequest[A, RepEnv](successAction: A => ZIO[RepEnv, Throwable, Long])
                             (request: Request)
                             (using aEncoder: Encoder[A])
                             (using aDecoder: Decoder[A]): ZIO[RepEnv, Throwable, Response] =
    for {
      eitherCompany <- request.bodyAsString.map(decode[A])
      response <- handleEither[Long, A, RepEnv](successAction)(eitherCompany)
    } yield response

  def genericGetRequest[A, RepEnv](getAction: String => ZIO[RepEnv, Throwable, A])
                                  (key: String)
                                  (using aEncoder: Encoder[A]): ZIO[RepEnv, Throwable, Response] =
    for {
      _ <- getAction(key)
    } yield Response.ok
