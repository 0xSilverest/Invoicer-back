package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.models.User
import dev.silverest.invoicerback.repositories.UserRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.JwtClaim

class UserHandler:
  val backendLayer: ZLayer[ZEnv, Nothing, UserRepository.Env] = UserRepository.live

  val publicEndpoints = Http.collectZIO[Request] {
      case req @ Method.POST -> _ / "signup" =>
        for {
          eUser <- req.bodyAsString.map(decode[User])
          response <-
            eUser match
              case Right(user) =>
                UserRepository.insert(user)
                Response.ok
              case Left(error) => Response.fromHttpError(HttpError.BadRequest(error.getMessage))
        } yield response

    }

  // Will require authentication
  // and mostly will be used by the admin
  def privateEndpoints (claim: JwtClaim) = Http.collectZIO[Request] {
      case Method.GET -> _ / "users" =>
        for {
          users <- UserRepository.getAll
        } yield Response.json(users.asJson.toString)

      case Method.GET -> _ / "users" / id =>
        for {
          user <- UserRepository.get(id)
        } yield Response.json(user.asJson.toString)

      case req @ Method.PUT -> _ / "users" =>
        for {
          eUser <- req.bodyAsString.map(decode[User])
          response <- eUser match
            case Right(user) =>
              // TODO: Add rules to unmodifiable fields
              UserRepository.update(user)
              Response.ok
            case Left(error) => Response.fromHttpError(HttpError.BadRequest(error.getMessage))
        } yield response

      case Method.DELETE -> _ / "users" / id =>
        for {
          _ <- UserRepository.delete(id)
        } yield Response.ok
    }