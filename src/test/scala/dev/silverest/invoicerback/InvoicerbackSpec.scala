package dev.silverest.invoicerback

import zio.test._
import zio.test.Assertion._
import zhttp.http._

object InvoicerbackSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("""InvoicerbackSpec""")(
    testM("200 ok") {
      checkAllM(Gen.fromIterable(List("text", "json"))) { uri =>
        val request = Request(Method.GET, URL(!! / uri))
        assertM(ZhttpService.app(request).map(_.status))(equalTo(Status.OK))
      }
    },
  )
}
