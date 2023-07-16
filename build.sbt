import Dependencies._

ThisBuild / scalaVersion     := "2.13.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "House finance",
    libraryDependencies ++= Seq(
      `cats-core`,
      `cats-effect`,
      fs2,
      `fs2-io`,
      `fs2-data-csv`,
      `sqlite-jdbc`,
      anorm,
      `macwire-macros` % Provided,
      scalatest % Test,
      `cats-effect-testing-scalatest` % Test,
      `liquibase` % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
