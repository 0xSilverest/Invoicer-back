package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.handlers.utils.HandlerUtils
import dev.silverest.invoicerback.models.User
import dev.silverest.invoicerback.repositories.UserAuthRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.JwtClaim

class UserHandler:
  val backendLayer: ZLayer[ZEnv, Nothing, UserAuthRepository.Env] = UserAuthRepository.live

  val publicEndpoints = Http.collectZIO[Request] {
    case req@Method.POST -> _ / "signup" =>
      HandlerUtils.successActionRequest(UserAuthRepository.insert)(req)
  }

  // Will require authentication
  // and mostly will be used by the admin
  def privateEndpoints (claim: JwtClaim) = Http.collectZIO[Request] {
      case Method.GET -> _ / "users" =>
        for {
          users <- UserAuthRepository.getAll
        } yield Response.json(s"${users.asJson}")

      case Method.GET -> _ / "users" / id =>
        for {
          user <- UserAuthRepository.findById(id)
        } yield Response.json(s"${user.asJson}")

      case req @ Method.PUT -> _ / "users" =>
        for {
          response <- req.bodyAsString
            .map(decode[User])
            .map{
              case Right(user) =>
                // TODO: Add rules to unmodifiable fields
                UserAuthRepository.update(user)
                Response.ok
              case Left(error) => Response.fromHttpError(HttpError.BadRequest(error.getMessage))
            }
        } yield response

      case Method.DELETE -> _ / "users" / id =>
        for {
          _ <- UserAuthRepository.delete(id)
        } yield Response.ok
    }
