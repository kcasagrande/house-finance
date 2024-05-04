package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Operation.Breakdown

trait OperationRepository
{
  def getByInterval(interval: LocalInterval): EitherT[IO, Throwable, Seq[Operation]]
  def getById(id: Operation.Id): EitherT[IO, Throwable, Option[Operation]]
  def save(operation: Operation): EitherT[IO, Throwable, Unit]
  def save(operations: Seq[Operation]): EitherT[IO, Throwable, Unit]
  def updateBreakdowns(operation: Operation.Id, breakdowns: Seq[Breakdown]): EitherT[IO, Throwable, Unit]
  def getAllCategories: EitherT[IO, Throwable, Set[String]]
}
