package dev.silverest.invoicerback.services

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import dev.silverest.invoicerback.repositories.UserRepository

class LoginService:
  case class Token(token: String)
  
  private val backendLayer: ZLayer[ZEnv, Nothing, UserRepository.Env] = UserRepository.live

  private val authenticator = Authenticator()

  def login(username: String, password: String) =
    UserRepository.findByUsername(username)
      .map {
        case Some(user) =>
          if user.validatePassword(password) then
            Response.json(s"${Token(authenticator.jwtEncode(user.username)).asJson}")
          else
            Response.fromHttpError(HttpError.BadRequest("Invalid password."))
        case None =>
          Response.fromHttpError(HttpError.BadRequest("User not found."))
      }


object LoginService:
    def apply(): LoginService = new LoginService()
