package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
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
  def withDatabase[T](test: EitherT[IO, Throwable, Database] => IO[T]): IO[T] = {
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
      testResult <- test(EitherT.liftF(IO.delay(Database(url = databaseUrl))))
      _ <- IO.delay(standByConnection.close())
    } yield {
      testResult
    }
  }
  
}
