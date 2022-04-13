package dev.silverest.invoicerback

import handlers._

import java.io.IOException
import zhttp.http._
import zio.blocking.Blocking

import java.io.IOException
import java.time.LocalDateTime
import zhttp.http.*
import zio.*
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}

import scala.util.Try

object Router:
  private val companyHandler = new CompanyHandler
  private val personHandler = new PersonHandler
  private val userHandler = new UserHandler
  private val authenticationHandler = new AuthenticationHandler

  val backendLayers =
    companyHandler.backendLayer
    ++ personHandler.backendLayer
    ++ userHandler.backendLayer

  private val publicRoutes =
    authenticationHandler.loginEndpoint
    ++ userHandler.publicEndpoints

  private def authenticationLayer[R, E] =
    authenticationHandler.authenticate[R, E](Http.forbidden("Not authorized"))


  // Those will require a blocking layer
  // which will be the authentication
  private val privateRoutes =
    authenticationLayer(userHandler.privateEndpoints)
    ++ authenticationLayer(personHandler.endpoints)
    ++ authenticationLayer(companyHandler.endpoints)

  val routes = publicRoutes ++ privateRoutes
