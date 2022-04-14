package dev.silverest.invoicerback.filters

import dev.silverest.invoicerback.services.{Authenticator, LoginService}
import dev.silverest.invoicerback.models.UserJwtDecode

import pdi.jwt.JwtClaim

import zhttp.http.*
import zio.*

import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class AuthenticationFilter:
  private val authenticator = Authenticator()
  private val jwtFilter = new JwtFilter

  // This will act as a filter in front of all the requests
  // to check authentication
  def authenticate[R, E](fail: HttpApp[R, E])(success: UserJwtDecode => HttpApp[R, E]): HttpApp[R, E] =
    Http.fromFunction[Request] {
      _.bearerToken
        .flatMap(token => authenticator.jwtDecode(token))
        .fold[HttpApp[R, E]](fail)
          (claim => jwtFilter.parseClaim(success)(decode[UserJwtDecode](claim.toJson)))
    }.flatten
