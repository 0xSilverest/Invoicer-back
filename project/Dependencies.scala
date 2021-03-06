import sbt._

object Dependencies {
  val ZioVersion   = "1.0.13"
  val ZHTTPVersion = "1.0.0.0-RC24"
  val circeVersion = "0.14.1"
  val quillVersion = "3.16.3.Beta2.5"
  val mysqlVersion = "8.0.28"
  val zioVersion   = "1.0.13"
  val tapirVersion = "1.0.0-M6"
  val zioLoggingVersion = "0.5.14"
  val postgresVersion  = "42.2.8"

  val `zio-core`    = "dev.zio" %% "zio" % zioVersion
  val `zio-logging` = "dev.zio" %% "zio-logging" % zioLoggingVersion
  val `zio-logging-slf4j` = "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion

  val `zio-http`      = "io.d11" %% "zhttp" % ZHTTPVersion
  val `zio-http-test` = "io.d11" %% "zhttp" % ZHTTPVersion % Test

  val `zio-test`     = "dev.zio" %% "zio-test"     % ZioVersion % Test
  val `zio-test-sbt` = "dev.zio" %% "zio-test-sbt" % ZioVersion % Test

  val `circe-core`    = "io.circe" %% "circe-core" % circeVersion
  val `circe-generic` = "io.circe" %% "circe-generic" % circeVersion
  val `circe-parser`  = "io.circe" %% "circe-parser" % circeVersion

  val `quill-jdbc-zio`  = "io.getquill" %% "quill-jdbc-zio" % quillVersion
  val `postgres-driver` = "org.postgresql" % "postgresql" % postgresVersion

  val `tapir-core`     = "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion
  val `tapir-zio`      = "com.softwaremill.sttp.tapir" %% "tapir-zio1" % tapirVersion
  val `tapir-zio-http` = "com.softwaremill.sttp.tapir" %% "tapir-zio1-http-server" % tapirVersion

  val `jwt-core` = "com.github.jwt-scala" %% "jwt-core" % "9.0.5"
  val `pbkdf2`   = "io.github.nremond" %% "pbkdf2-scala" % "0.6.9-SNAPSHOT"
}
