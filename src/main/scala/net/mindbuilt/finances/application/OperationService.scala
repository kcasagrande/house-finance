package net.mindbuilt.finances.application

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.application.OperationService.SearchCriterion
import net.mindbuilt.finances.business.LocalInterval.LocalIntervalBoundary
import net.mindbuilt.finances.business.Operation.Breakdown
import net.mindbuilt.finances.business.{Operation, OperationRepository}

import java.time.LocalDate

class OperationService(
  operationRepository: OperationRepository
) {
  def search(criteria: Seq[SearchCriterion]): EitherT[IO, Throwable, Seq[Operation]] =
    operationRepository.search(criteria)
  
  def registerOperation(operation: Operation): EitherT[IO, Throwable, Operation.Id] =
    ???
  
  def getAllCategories: EitherT[IO, Throwable, Set[String]] =
    operationRepository.getAllCategories
    
  def breakDown(operationId: Operation.Id, breakdown: Seq[Breakdown]): EitherT[IO, Throwable, Unit] =
    operationRepository.updateBreakdowns(operationId, breakdown)
}

object OperationService {
  sealed trait SearchCriterion
  object SearchCriterion {
    case class From(date: LocalDate) extends SearchCriterion
    case class To(date: LocalDate) extends SearchCriterion
  }
}