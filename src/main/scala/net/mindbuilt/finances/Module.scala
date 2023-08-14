package net.mindbuilt.finances

import cats.data.EitherT
import cats.effect.IO
import pureconfig.ConfigSource
import pureconfig.generic.auto._

trait Module
{
  lazy val configuration: EitherT[IO, Throwable, Configuration] = EitherT(IO.delay(
    ConfigSource.default.load[Configuration]
  ))
    .leftMap { configReaderFailures =>
      new Exception((configReaderFailures.head +: configReaderFailures.tail).map(_.description).mkString("\n"))
    }
}
