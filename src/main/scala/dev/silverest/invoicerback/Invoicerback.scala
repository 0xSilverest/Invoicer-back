package dev.silverest.invoicerback

import zhttp.http._
import zhttp.service.Server
import zio._

object Invoicerback extends App {
  val app: HttpApp[Any, Nothing] = Http.collect[Request] {
    case Method.GET -> _ / "text" => Response.text("Hello World!")
    case Method.GET -> _ / "json" => Response.json("""{"greetings": "Hello World!"}""")
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, app).exitCode
}
