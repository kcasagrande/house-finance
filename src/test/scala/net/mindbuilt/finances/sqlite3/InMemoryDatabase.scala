package net.mindbuilt.finances.sqlite3

import cats.effect.IO
import liquibase.Scope
import liquibase.Scope.ScopedRunner
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.resource.{DirectoryResourceAccessor, SearchPathResourceAccessor}
import org.scalatest.Suite

import java.nio.file.Path
import java.sql.{Connection, DriverManager}
import java.util.UUID

trait InMemoryDatabase
{ self: Suite =>
  def withDatabaseSync(test: Connection => Any): Unit = {
    val databaseUrl = s"jdbc:sqlite:file:memory-${UUID.randomUUID().toString}?mode=memory&cache=shared"
    implicit val connection: Connection = {
      val connection = DriverManager.getConnection(databaseUrl)
      connection.setAutoCommit(false)
      connection
    }

    val resourceAccessor: SearchPathResourceAccessor = new SearchPathResourceAccessor(
      new DirectoryResourceAccessor(Path.of("src/main/resources/liquibase"))
    )
    Scope.child(Scope.Attr.resourceAccessor.name(), resourceAccessor, new ScopedRunner[Unit] {
      override def run(): Unit = {
        val commandScope = new CommandScope("update")
          .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
          .addArgumentValue("url", databaseUrl)
        commandScope.execute()
      }
    })
    test(connection)
    connection.close()
  }

  def withDatabase[T](test: Database => IO[T]): IO[T] = {
    val databaseUrl = s"jdbc:sqlite:file:memory-${UUID.randomUUID().toString}?mode=memory&cache=shared"
    def liquibaseUpdate(): Unit = {
      val resourceAccessor: SearchPathResourceAccessor = new SearchPathResourceAccessor(
        new DirectoryResourceAccessor(Path.of("src/main/resources/liquibase"))
      )
      Scope.child(Scope.Attr.resourceAccessor.name(), resourceAccessor, new ScopedRunner[Unit] {
        override def run(): Unit = {
          val commandScope = new CommandScope("update")
            .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
            .addArgumentValue("url", databaseUrl)
          commandScope.execute()
        }
      })
    }
    for {
      _ <- IO(println("Using database " + databaseUrl + "."))
      standByConnection <- IO.delay(DriverManager.getConnection(databaseUrl))
      _ <- IO(liquibaseUpdate())
      testResult <- test(Database(url = databaseUrl))
      _ <- IO.delay(standByConnection.close())
    } yield {
      testResult
    }
  }
  
}
