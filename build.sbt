import Dependencies._

// give the user a nice default project!
ThisBuild / organization := "dev.silverest"
ThisBuild / version := "1.0.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(BuildHelper.stdSettings)
  .settings(
    name := "Invoicer-Back",

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),

    libraryDependencies ++= Seq(
      `zio-core`,  `zio-logging`,
      `zio-test`, `zio-test-sbt`,
      `zio-http`, `zio-http-test`,
      `circe-core`, `circe-generic`, `circe-parser`,
      `postgres-driver`, `quill-jdbc-zio`,
      `jwt-core`, `pbkdf2`,
      `tapir-core`, `tapir-zio`, `tapir-zio-http`)
  )
  .settings(
    Docker / version          := version.value,
    Compile / run / mainClass := Option("dev.silverest.invoicerback.Invoicerback"),
  )

addCommandAlias("fmt", "scalafmt; Test / scalafmt; sFix;")
addCommandAlias("fmtCheck", "scalafmtCheck; Test / scalafmtCheck; sFixCheck")
addCommandAlias("sFix", "scalafix OrganizeImports; Test / scalafix OrganizeImports")
addCommandAlias("sFixCheck", "scalafix --check OrganizeImports; Test / scalafix --check OrganizeImports")
