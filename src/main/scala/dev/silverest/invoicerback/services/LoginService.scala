package dev.silverest.invoicerback.services

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import dev.silverest.invoicerback.repositories.UserAuthRepository

class LoginService:
  private val backendLayer: ZLayer[ZEnv, Nothing, UserAuthRepository.Env] = UserAuthRepository.live

  private val authenticator = Authenticator()

  def login(username: String, password: String) =
    UserAuthRepository.findByUsername(username)
      .map {
        case Some(user) =>
          if user.password == password then
            Response.json(s"${authenticator.jwtEncode(user.username).asJson}")
          else
            Response.fromHttpError(HttpError.BadRequest("Invalid password."))
        case None =>
          Response.fromHttpError(HttpError.BadRequest("User not found."))
      }


object LoginService:
    def apply(): LoginService = new LoginService()
