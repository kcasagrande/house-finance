import sbt._

object Dependencies {
  lazy val `cats-core` = "org.typelevel" %% "cats-core" % "2.9.0"
  lazy val `cats-effect` = "org.typelevel" %% "cats-effect" % "3.5.1"
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.16"
  lazy val `cats-effect-testing-scalatest` = "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0"
  lazy val fs2 = "co.fs2" %% "fs2-core" % "3.7.0"
  lazy val `fs2-io` = "co.fs2" %% "fs2-io" % "3.7.0"
  lazy val `fs2-data-csv` = "org.gnieh" %% "fs2-data-csv" % "1.7.1"
}
