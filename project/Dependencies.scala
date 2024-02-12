import sbt._

object Dependencies {
  lazy val `cats-core` = "org.typelevel" %% "cats-core" % "2.9.0"
  lazy val `cats-effect` = "org.typelevel" %% "cats-effect" % "3.5.1"
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.16"
  lazy val `cats-effect-testing-scalatest` = "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0"
  lazy val fs2 = "co.fs2" %% "fs2-core" % "3.7.0"
  lazy val `fs2-io` = "co.fs2" %% "fs2-io" % "3.7.0"
  lazy val `fs2-data-csv` = "org.gnieh" %% "fs2-data-csv" % "1.7.1"
  lazy val `sqlite-jdbc` = "org.xerial" % "sqlite-jdbc" % "3.42.0.0"
  lazy val anorm = "org.playframework.anorm" %% "anorm" % "2.7.0"
  lazy val `macwire-macros` = "com.softwaremill.macwire" %% "macros" % "2.5.8"
  lazy val `liquibase` = "org.liquibase" % "liquibase-core" % "4.23.0"
  lazy val http4sVersion = "0.23.23"
  lazy val `http4s-core` = "org.http4s" %% "http4s-core" % http4sVersion
  lazy val `http4s-dsl` = "org.http4s" %% "http4s-dsl" % http4sVersion
  lazy val `http4s-ember-server` = "org.http4s" %% "http4s-ember-server" % http4sVersion
  lazy val `http4s-circe` = "org.http4s" %% "http4s-circe" % http4sVersion
  lazy val `http4s-client` = "org.http4s" %% "http4s-client" % http4sVersion
  lazy val `circe-literal` = "io.circe" %% "circe-literal" % "0.14.5"
  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.17.4"
}
