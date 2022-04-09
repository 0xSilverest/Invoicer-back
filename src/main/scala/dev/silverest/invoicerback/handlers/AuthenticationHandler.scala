package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.services.{Authenticator, LoginService}

import pdi.jwt.JwtClaim

import zhttp.http.*
import zio.*

import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class AuthenticationHandler:
  private val authenticator = Authenticator
  private val loginService = LoginService

  // This will act as a filter in front of all the requests
  // to check authentication
  def authenticate[R, E](fail: HttpApp[R, E], success: JwtClaim => HttpApp[R, E]): HttpApp[R, E] =
    Http.fromFunction[Request] {
      _.headers
        .toList
        .filter(_._1 == "Authorization")
        .map(header => {
          val maybeClaim: Option[JwtClaim] = authenticator.jwtDecode(header._2)
          maybeClaim match
            case Some(claim) => success(claim)
            case None => fail
        }).head
    }.flatten

  val loginEndpoint =
    Http.collectZIO[Request] {
      case req @ Method.GET -> _ / "login" =>
        for {
          eCreds <- req.bodyAsString.map(decode[LoginRequest])
          response <- eCreds match
            case Right(creds) => loginService.login(creds.username, creds.password)
            case Left(error) => ZIO.fail(error)
        } yield Response.json(response.asJson)
    }

  case class LoginRequest(username: String, password: String)