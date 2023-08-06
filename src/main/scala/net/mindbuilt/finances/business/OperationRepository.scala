package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait OperationRepository
{
  def getByInterval(interval: LocalInterval): EitherT[IO, Throwable, Seq[Operation]]
  def save(operation: Operation): EitherT[IO, Throwable, Unit]
}
