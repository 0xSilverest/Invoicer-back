package dev.silverest.invoicerback.handlers.utils

import dev.silverest.invoicerback.repositories.CompanyRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

object HandlerUtils:
  def handleEither[A, B, RepEnv](successAction: B => ZIO[RepEnv, Throwable, A]): Either[Error, B] => ZIO[RepEnv, Throwable, A] =
    eitherA =>
     eitherA match
      case Right(a) => successAction(a)
      case Left(e) => ZIO.fail(e)


  def successActionRequest[A, RepEnv](successAction: A => ZIO[RepEnv, Throwable, Long])
                             (request: Request)
                             (using aEncoder: Encoder[A])
                             (using aDecoder: Decoder[A]): ZIO[RepEnv, Throwable, Response] =
    for {
      eitherCompany <- request.bodyAsString.map(decode[A])
      res <- HandlerUtils.handleEither[Long, A, RepEnv](successAction)(eitherCompany)
    } yield Response.json(res.asJson.toString)