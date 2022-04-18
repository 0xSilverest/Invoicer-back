package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.handlers.utils.*
import dev.silverest.invoicerback.models.User
import dev.silverest.invoicerback.models.UserJwtDecode
import dev.silverest.invoicerback.repositories.UserRepository
import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.JwtClaim

class UserHandler:
  private type RepEnv = UserRepository.Env
  val backendLayer: ZLayer[ZEnv, Nothing, RepEnv] = UserRepository.live

  val publicEndpoints = Http.collectZIO[Request] {
    case request @ Method.POST -> _ / "signup" =>
      request.bodyAsString.map(decode[User])
        .flatMap {
          case Right(user) =>
            UserRepository.containsNot(user.username)
            .flatMap {
              case true => UserRepository.containsNotEmail(user.email).flatMap {
                case true => HandlerUtils.successActionHandler(UserRepository.insert)(user)
                case false => ErrorHandler.conflict("Email already used!")
              }
              case false => ErrorHandler.conflict("Username is already used")
            }
          case Left(e) => ErrorHandler.unprocessableEntity(s"$e")
        }
    }

  // Will require authentication
  // and mostly will be used by the admin
  def privateEndpoints (userDecoded: UserJwtDecode) = Http.collectZIO[Request] {
      case Method.GET -> _ / "users" =>
        for {
          users <- UserRepository.getAll
        } yield Response.json(s"${users.asJson}")

      case Method.GET -> _ / "users" / username =>
        for {
          user <- UserRepository.findByUsername(username)
        } yield Response.json(s"${user.asJson}")

      case req @ Method.PUT -> _ / "users" =>
        for {
          response <- req.bodyAsString
            .map(decode[User])
            .map{
              case Right(user) =>
                // TODO: Add rules to unmodifiable fields
                UserRepository.update(user)
                Response.ok
              case Left(error) => Response.fromHttpError(HttpError.BadRequest(error.getMessage))
            }
        } yield response

      case Method.DELETE -> _ / "users" / username =>
        for {
          _ <- UserRepository.delete(username)
        } yield Response.ok
    }
