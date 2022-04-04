package dev.silverest.invoicerback

import zhttp.http.*
import zhttp.service.Server
import zio._

import scala.util.Try
import zhttp.service.server.ServerChannelFactory
import zhttp.service.EventLoopGroup

object Invoicerback extends zio.App:
  private val PORT = 8090

  private val server =
    Server.port(PORT) ++ 
      Server.app(Router.routes)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    val env = ServerChannelFactory.auto ++
      EventLoopGroup.auto(nThreads) ++
      Router.backendLayers

    server
      .make
      .use(_ =>
        console.putStrLn(s"Server started on port $PORT")
          *> ZIO.never,
      )
      .provideCustomLayer(env)
      .exitCode 

