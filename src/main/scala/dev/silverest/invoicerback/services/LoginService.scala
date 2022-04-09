package dev.silverest.invoicerback.services

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import dev.silverest.invoicerback.repositories.UserRepository

class LoginService:
  private val backendLayer: ZLayer[ZEnv, Nothing, UserRepository.Env] = UserRepository.live

  private val authenticator = Authenticator

  def login(username: String, password: String): Either[String, String] =
    for {
      mUser <- UserRepository.findByUsername(username)
      eLoggedUser <-
        mUser match
          case Some(user) =>
            if user.password == password then
              Right(authenticator.jwtEncode(user.username))
            else
              Left("Invalid password")
          case None =>
            Left("User not found")
    } yield eLoggedUser


object LoginService:
    def apply(): LoginService = new LoginService()