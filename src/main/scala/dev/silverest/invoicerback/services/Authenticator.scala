package dev.silverest.invoicerback.services

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zhttp.http.*
import zhttp.service.Server
import zio.*

import java.time.Clock

class Authenticator:

  // Very secrety
  private val SECRET_KEY = "secret"

  implicit val clock: Clock = Clock.systemUTC

  def jwtEncode(username: String): String =
    val json = s"""{"username": "$username"}"""
    val claim = JwtClaim(json).issuedNow.expiresIn(3600)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)

  def jwtDecode(token: String): Option[JwtClaim] =
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption

object Authenticator:
  def apply() = new Authenticator
