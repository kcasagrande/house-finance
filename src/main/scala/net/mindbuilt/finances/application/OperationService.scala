package net.mindbuilt.finances.application

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.LocalInterval.LocalIntervalBoundary
import net.mindbuilt.finances.business.{Operation, OperationRepository}

import java.time.LocalDate

class OperationService(
  operationRepository: OperationRepository
) {
  def getAllOperations(from: LocalDate, to: LocalDate): EitherT[IO, Throwable, Seq[Operation]] =
    operationRepository.getByInterval(from to to)
  
  def getAllCategories: EitherT[IO, Throwable, Set[String]] =
    operationRepository.getAllCategories
}