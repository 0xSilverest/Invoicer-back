package dev.silverest.invoicerback.filters

import zhttp.http.*
import dev.silverest.invoicerback.models.UserJwtDecode

class JwtFilter:

  def parseClaim[R, E](success: UserJwtDecode => HttpApp[R, E]): Either[io.circe.Error, UserJwtDecode] => HttpApp[R, E] =
    case Right(r) => success(r)
    case Left(e) => Http.badRequest("Couldn't decode jwt claim")

