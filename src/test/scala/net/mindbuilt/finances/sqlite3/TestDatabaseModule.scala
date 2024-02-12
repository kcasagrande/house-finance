package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import liquibase.Scope
import liquibase.Scope.ScopedRunner
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.resource.{DirectoryResourceAccessor, SearchPathResourceAccessor}
import pureconfig.generic.auto._

import java.nio.file.Path
import java.util.UUID

trait TestDatabaseModule
{
  lazy val database: EitherT[IO, Throwable, Database] =
    for {
      randomId <- EitherT.pure[IO, Throwable](UUID.randomUUID())
      url <- EitherT.pure[IO, Throwable](s"jdbc:sqlite:file:memory-${randomId}?mode=memory&cache=shared")
      resourceAccessor <- EitherT.pure[IO, Throwable](new SearchPathResourceAccessor(
        new DirectoryResourceAccessor(Path.of("src/main/resources/liquibase"))
      ))
      _ <- EitherT.liftF(IO.delay(Scope.child(
        Scope.Attr.resourceAccessor.name(),
        resourceAccessor,
        new ScopedRunner[Unit] {
          override def run(): Unit =
            new CommandScope("update")
              .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
              .addArgumentValue("url", url)
              .execute()
        }
      )))
      db <- EitherT.pure[IO, Throwable](Database(url = url))
    } yield {
      db
    }
}
