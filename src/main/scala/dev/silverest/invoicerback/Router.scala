package dev.silverest.invoicerback

import handlers._

import java.io.IOException
import zhttp.http._
import zio.blocking.Blocking

import java.io.IOException
import java.time.LocalDateTime
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._

import scala.util.Try

object Router:
    private val companyHandler = new CompanyHandler
    private val personHandler = new PersonHandler

    val routes = companyHandler.endpoints ++ personHandler.endpoints
    val backendLayers = companyHandler.backendLayer ++ personHandler.backendLayer