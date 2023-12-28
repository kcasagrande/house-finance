package net.mindbuilt.finances.api.v1

import cats.effect.IO
import io.circe._
import io.circe.literal._
import io.circe.syntax._
import net.mindbuilt.finances.api.v1.OperationController._
import net.mindbuilt.finances.application.OperationService
import net.mindbuilt.finances.business.Operation
import net.mindbuilt.finances.business.Operation.Breakdown
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, QueryParamDecoder}

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class OperationController(
  operationService: OperationService
) extends CirceEntityEncoder
{
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root :? From(from) +& To(to) =>
      operationService.getAllOperations(from, to)
        .value
        .flatMap {
          case Left(throwable) => InternalServerError(throwable.getMessage)
          case Right(operations) => Ok(operations.map(_.asJson))
        }

    case GET -> Root / "categories" => operationService.getAllCategories
      .value
      .flatMap {
        case Left(throwable) => InternalServerError(throwable.getMessage)
        case Right(categories) => Ok(categories.map(_.asJson))
      }
  }
}

object OperationController {
  implicit val localDateQueryParamDecoder: QueryParamDecoder[LocalDate] = QueryParamDecoder[String].map(ISO_LOCAL_DATE.parse(_)).map(LocalDate.from)
  object From extends QueryParamDecoderMatcher[LocalDate]("from")
  object To extends QueryParamDecoderMatcher[LocalDate]("to")
  
  implicit val operationEncoder: Encoder[Operation] = Encoder.instance {
    case cardOperation: Operation.ByCard => cardOperationEncoder.apply(cardOperation)
    case checkOperation: Operation.ByCheck => checkOperationEncoder.apply(checkOperation)
    case debitOperation: Operation.ByDebit => debitOperationEncoder.apply(debitOperation)
    case transferOperation: Operation.ByTransfer => transferOperationEncoder.apply(transferOperation)
  }

  implicit private val breakdownEncoder: Encoder[Breakdown] = Encoder.instance {
    (breakdown: Breakdown) =>
      json"""{
               "credit": ${breakdown.credit.value},
               "category": ${breakdown.category},
               "comment": ${breakdown.comment},
               "supplier": ${breakdown.supplier.map(_.toString)}
             }"""
  }
  
  private val cardOperationEncoder: Encoder[Operation.ByCard] = Encoder.instance {
    (operation: Operation.ByCard) =>
      json"""{
               "type": "card",
               "id": ${operation.id.toString},
               "card": ${operation.card},
               "reference": ${operation.reference},
               "label": ${operation.label},
               "operationDate": ${operation.operationDate},
               "valueDate": ${operation.valueDate},
               "accountDate": ${operation.accountDate},
               "breakdown": ${operation.breakdown.map(_.asJson)}
             }"""
  }

  private val checkOperationEncoder: Encoder[Operation.ByCheck] = Encoder.instance {
    (operation: Operation.ByCheck) =>
      json"""{
               "type": "check",
               "id": ${operation.id.toString},
               "account": ${operation.account.toString},
               "number": ${operation.number},
               "label": ${operation.label},
               "operationDate": ${operation.operationDate},
               "valueDate": ${operation.valueDate},
               "accountDate": ${operation.accountDate},
               "breakdown": ${operation.breakdown.map(_.asJson)}
             }"""
  }

  private val debitOperationEncoder: Encoder[Operation.ByDebit] = Encoder.instance {
    (operation: Operation.ByDebit) =>
      json"""{
               "type": "debit",
               "id": ${operation.id.toString},
               "account": ${operation.account.toString},
               "reference": ${operation.reference},
               "label": ${operation.label},
               "operationDate": ${operation.operationDate},
               "valueDate": ${operation.valueDate},
               "accountDate": ${operation.accountDate},
               "breakdown": ${operation.breakdown.map(_.asJson)}
             }"""
  }
  
  private val transferOperationEncoder: Encoder[Operation.ByTransfer] = Encoder.instance {
    (operation: Operation.ByTransfer) =>
      json"""{
               "type": "transfer",
               "id": ${operation.id.toString},
               "account": ${operation.account.toString},
               "reference": ${operation.reference},
               "label": ${operation.label},
               "operationDate": ${operation.operationDate},
               "valueDate": ${operation.valueDate},
               "accountDate": ${operation.accountDate},
               "otherParty": ${operation.otherParty.map(_.toString)},
               "breakdown": ${operation.breakdown.map(_.asJson)}
             }"""
  }
}
