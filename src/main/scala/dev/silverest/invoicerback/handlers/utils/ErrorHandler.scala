package dev.silverest.invoicerback.handlers.utils

import zio.*
import zhttp.*
import zhttp.http.*

object ErrorHandler:

  def handleError(httpError: HttpError) = 
    ZIO.succeed(httpError.toResponse)

  def conflict(message: String) =
    handleError(HttpError.Conflict(message))

  def unprocessableEntity(message: String) =
    handleError(HttpError.UnprocessableEntity(message))
