package net.mindbuilt.finances.api.v1

import cats.effect.IO
import io.circe._
import io.circe.literal._
import io.circe.syntax._
import net.mindbuilt.finances.Cents
import net.mindbuilt.finances.api.v1.OperationController._
import net.mindbuilt.finances.application.OperationService
import net.mindbuilt.finances.business.Operation.Breakdown
import net.mindbuilt.finances.business.{Holder, Iban, Operation}
import org.http4s.circe.{CirceEntityEncoder, jsonOf}
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, HttpRoutes, QueryParamDecoder}

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.util.UUID
import scala.util.Try

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

    case request @ POST -> Root =>
      for {
        operation <- request.as[Operation]
        response <- operationService.registerOperation(operation)
          .value
          .flatMap {
            case Left(throwable) => InternalServerError(throwable.getMessage)
            case Right(id) => Ok(id.asJson)
          }
      } yield response

    case request @ PUT -> Root / OperationId(operation) / "breakdown" =>
      for {
        breakdown <- request.as[Seq[Breakdown]]
        response <- operationService.breakDown(operation, breakdown)
          .value
          .flatMap {
            case Left(throwable) => InternalServerError(throwable.getMessage)
            case Right(_) => Ok(())
          }
      } yield {
        response
      }
  }
}

object OperationController {
  implicit val localDateQueryParamDecoder: QueryParamDecoder[LocalDate] = QueryParamDecoder[String].map(ISO_LOCAL_DATE.parse(_)).map(LocalDate.from)
  private object From extends QueryParamDecoderMatcher[LocalDate]("from")
  private object To extends QueryParamDecoderMatcher[LocalDate]("to")
  private object OperationId {
    def unapply(pathParameter: String): Option[Operation.Id] =
      Try(UUID.fromString(pathParameter)).toOption
  }

  implicit val operationDecoder: Decoder[Operation] = (c: HCursor) => {
    c.downField("method").as[String].flatMap {
      case "card" => for {
        id <- c.downField("id").as[Option[UUID]].map(_.getOrElse(UUID.randomUUID()))
        card <- c.downField("card").as[String]
        reference <- c.downField("reference").as[Option[String]]
        label <- c.downField("label").as[String]
        credit <- c.downField("credit").as[Cents]
        operationDate <- c.downField("operationDate").as[LocalDate]
        valueDate <- c.downField("valueDate").as[LocalDate]
        accountDate <- c.downField("accountDate").as[LocalDate]
      } yield {
        Operation.ByCard(
          id,
          card,
          reference,
          label,
          credit,
          operationDate,
          valueDate,
          accountDate
        )
      }
      case "check" => for {
        id <- c.downField("id").as[Option[UUID]].map(_.getOrElse(UUID.randomUUID()))
        account <- c.downField("account").as[Iban]
        checkNumber <- c.downField("checkNumber").as[String]
        label <- c.downField("label").as[String]
        credit <- c.downField("credit").as[Cents]
        operationDate <- c.downField("operationDate").as[LocalDate]
        valueDate <- c.downField("valueDate").as[LocalDate]
        accountDate <- c.downField("accountDate").as[LocalDate]
      } yield {
        Operation.ByCheck(
          id,
          account,
          checkNumber,
          label,
          credit,
          operationDate,
          valueDate,
          accountDate
        )
      }
      case "debit" => for {
        id <- c.downField("id").as[Option[UUID]].map(_.getOrElse(UUID.randomUUID()))
        account <- c.downField("account").as[Iban]
        reference <- c.downField("reference").as[Option[String]]
        label <- c.downField("label").as[String]
        credit <- c.downField("credit").as[Cents]
        operationDate <- c.downField("operationDate").as[LocalDate]
        valueDate <- c.downField("valueDate").as[LocalDate]
        accountDate <- c.downField("accountDate").as[LocalDate]
      } yield {
        Operation.ByDebit(
          id,
          account,
          reference,
          label,
          credit,
          operationDate,
          valueDate,
          accountDate
        )
      }
      case "transfer" => for {
        id <- c.downField("id").as[Option[UUID]].map(_.getOrElse(UUID.randomUUID()))
        account <- c.downField("account").as[Iban]
        reference <- c.downField("reference").as[Option[String]]
        label <- c.downField("label").as[String]
        credit <- c.downField("credit").as[Cents]
        operationDate <- c.downField("operationDate").as[LocalDate]
        valueDate <- c.downField("valueDate").as[LocalDate]
        accountDate <- c.downField("accountDate").as[LocalDate]
        otherParty <- c.downField("otherParty").as[Option[Iban]]
      } yield {
        Operation.ByTransfer(
          id,
          account,
          reference,
          label,
          credit,
          operationDate,
          valueDate,
          accountDate,
          otherParty
        )
      } 
      case anythingElse => Left(DecodingFailure("Unknown payment method %s".format(anythingElse), c.history))
    }
  }

  implicit val operationEntityDecoder: EntityDecoder[IO, Operation] = jsonOf[IO, Operation]

  implicit val breakdownDecoder: Decoder[Breakdown] = (c: HCursor) => {
    for {
      credit <- c.downField("credit").as[Int].map(Cents)
      category <- c.downField("category").as[Option[String]]
      comment <- c.downField("comment").as[Option[String]]
      supplier <- c.downField("comment").as[Option[Holder.Id]]
    } yield {
      Breakdown(credit, category, comment, supplier)
    }
  }

  implicit val breakdownsEntityDecoder: EntityDecoder[IO, Seq[Breakdown]] = jsonOf[IO, Seq[Breakdown]]

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
               "method": "card",
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
               "method": "check",
               "id": ${operation.id.toString},
               "account": ${operation.account.toString},
               "checkNumber": ${operation.checkNumber},
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
               "method": "debit",
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
               "method": "transfer",
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
