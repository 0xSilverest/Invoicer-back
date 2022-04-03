package dev.silverest.invoicerback

import handlers._

import java.io.IOException
import zhttp.http.*
import zio.blocking.Blocking

import java.io.IOException
import java.time.LocalDateTime
import zhttp.http.*
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.*

import scala.util.Try

object Router:
    private val companyHandler = new CompanyHandler

    val routes = companyHandler.endpoints
    val backendLayers = companyHandler.companyBackendLayer