package dev.silverest.invoicerback.filters

import zhttp.http.*
import dev.silverest.invoicerback.models.UserJwtDecode

class JwtFilter:
  
  //def authenticate[R, E](fail: HttpApp[R, E])(success: Either[Error, UserJwtDecode] => HttpApp[R, E]): HttpApp[R, E] =
  //  Http.fromFunction[Request] {
  //    _.bearerToken
  //      .flatMap(token => authenticator.jwtDecode(token))
  //      .fold[HttpApp[R, E]](fail)(claim => success(decode[UserJwtDecode](claim.toJson)))
  //  }.flatten

  def parseClaim[R, E](success: UserJwtDecode => HttpApp[R, E]): Either[io.circe.Error, UserJwtDecode] => HttpApp[R, E] =
    case Right(r) => success(r)
    case Left(e) => Http.badRequest("Couldn't decode jwt claim")

