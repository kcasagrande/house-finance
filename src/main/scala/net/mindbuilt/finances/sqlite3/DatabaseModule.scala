package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.Configuration

trait DatabaseModule {
  def configuration: EitherT[IO, Throwable, Configuration]
  lazy val database: EitherT[IO, Throwable, Database] = configuration
    .map(_.database)
    .map("jdbc:sqlite:file:" + _)
    .map(Database)
}
