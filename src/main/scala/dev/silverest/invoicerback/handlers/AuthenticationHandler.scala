package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.services.{Authenticator, LoginService}
import dev.silverest.invoicerback.models.UserJwtDecode

import utils._

import pdi.jwt.JwtClaim

import zhttp.http.*
import zio.*

import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class AuthenticationHandler:
  private val authenticator = Authenticator()
  private val loginService = LoginService()

  // This will act as a filter in front of all the requests
  // to check authentication
  def authenticate[R, E](fail: HttpApp[R, E])(success: Either[Error, UserJwtDecode] => HttpApp[R, E]): HttpApp[R, E] =
    Http.fromFunction[Request] {
      _.bearerToken
        .flatMap(token => authenticator.jwtDecode(token))
        .fold[HttpApp[R, E]](fail)(claim => success(decode[UserJwtDecode](claim.toJson)))
    }.flatten

  val loginEndpoint =
    Http.collectZIO[Request] {
      case req @ Method.POST -> _ / "login" =>
        for {
          response <- req.bodyAsString
            .map(decode[LoginRequest])
            .flatMap {
              case Right(creds) => loginService.login(creds.username, creds.password)
              case Left(e) => ErrorHandler.unprocessableEntity(s"$e")
            }
        } yield response
    }

  case class LoginRequest(username: String, password: String)
