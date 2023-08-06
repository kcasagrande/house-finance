package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{LocalInterval, Operation}
import net.mindbuilt.finances.sqlite3.OperationRepository._
import net.mindbuilt.finances.{business => port}

import java.sql.Connection
import scala.language.implicitConversions

class OperationRepository(implicit val connection: Connection)
  extends port.OperationRepository
{
  override def getByInterval(interval: LocalInterval): EitherT[IO, Throwable, Seq[Operation]] =
    EitherT.liftF(IO {
      SQL(
        """SELECT * FROM operation
          |WHERE (
          |operation_date >= {start}
          |OR value_date >= {start}
          |OR account_date >= {start}
          |) AND (
          |operation_date <= {end}
          |OR value_date <= {end}
          |OR account_date <= {end}
          |)""".stripMargin)
        .on(interval:_*)
        .as(operationParser.*)
    }
  )

  override def save(operation: Operation): EitherT[IO, Throwable, Unit] =
    operation match {
      case cardOperation: Operation.ByCard => saveCardOperation(cardOperation)
      case checkOperation: Operation.ByCheck => saveCheckOperation(checkOperation)
      case debitOperation: Operation.ByDebit => saveDebitOperation(debitOperation)
      case transferOperation: Operation.ByTransfer => saveTransferOperation(transferOperation)
    }
  
  private def saveCardOperation(operation: Operation.ByCard): EitherT[IO, Throwable, Unit] =
    EitherT.liftF(IO {
      SQL(
        """INSERT INTO "card_operation"("id", "card", "reference", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {card}, {reference}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin
      )
        .on(operation:_*)
        .execute()
    })
      .map(_ => ())
      
  private def saveCheckOperation(operation: Operation.ByCheck): EitherT[IO, Throwable, Unit] =
    EitherT.liftF(IO {
      SQL(
        """INSERT INTO "check_operation"("id", "account_country_code", "account_check_digits", "account_bban", "number", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {number}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin
      )
        .on(operation:_*)
        .execute()
    })
      .map(_ => ())

  private def saveDebitOperation(operation: Operation.ByDebit): EitherT[IO, Throwable, Unit] =
    EitherT.liftF(IO {
      SQL(
        """INSERT INTO "debit_operation"("id", "account_country_code", "account_check_digits", "account_bban", "reference", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {reference}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin
      )
        .on(operation:_*)
        .execute()
    })
      .map(_ => ())

  private def saveTransferOperation(operation: Operation.ByTransfer): EitherT[IO, Throwable, Unit] =
    EitherT.liftF(IO {
      SQL(
        """INSERT INTO "transfer_operation"("id", "account_country_code", "account_check_digits", "account_bban", "reference", "label", "operation_date", "value_date", "account_date", "other_party_country_code", "other_party_check_digits", "other_party_bban")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {reference}, {label}, {operation_date}, {value_date}, {account_date}, {other_party_country_code}, {other_party_check_digits}, {other_party_bban})
          |""".stripMargin
      )
        .on(operation:_*)
        .execute()
    })
      .map(_ => ())
}

object OperationRepository {
  implicit def intervalToNamedParameters(interval: LocalInterval): Seq[NamedParameter] =
    namedParameters(
      "start" -> interval.start,
      "end" -> interval.end
    )
    
  implicit def cardOperationToNamedParameters(operation: Operation.ByCard): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "card" -> operation.card,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "operation_date" -> operation.operationDate,
      "value_date" -> operation.valueDate,
      "account_date" -> operation.accountDate
    )

  implicit def checkOperationToNamedParameters(operation: Operation.ByCheck): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "number" -> operation.number,
      "label" -> operation.label,
      "operation_date" -> operation.operationDate,
      "value_date" -> operation.valueDate,
      "account_date" -> operation.accountDate
    )

  implicit def debitOperationToNamedParameters(operation: Operation.ByDebit): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "operation_date" -> operation.operationDate,
      "value_date" -> operation.valueDate,
      "account_date" -> operation.accountDate
    )

  implicit def transferOperationToNamedParameters(operation: Operation.ByTransfer): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "operation_date" -> operation.operationDate,
      "value_date" -> operation.valueDate,
      "account_date" -> operation.accountDate,
      "other_party_country_code" -> operation.otherParty.map(_.countryCode),
      "other_party_check_digits" -> operation.otherParty.map(_.checkDigits),
      "other_party_bban" -> operation.otherParty.map(_.bban)
    )

  val operationParser: RowParser[Operation] =
    str("type")
      .flatMap {
        case "card" => cardOperationParser
        case "check" => checkOperationParser
        case "debit" => debitOperationParser
        case "transfer" => transferOperationParser
      }
      
  private val cardOperationParser: RowParser[Operation.ByCard] =
    for {
      id <- uuid("id")
      label <- str("label")
      card <- str("card")
      reference <- str("reference")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate)
    }
    
  private val checkOperationParser: RowParser[Operation.ByCheck] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      number <- str("number")
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate)
    }

  private val debitOperationParser: RowParser[Operation.ByDebit] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference")
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate)
    }

  private val transferOperationParser: RowParser[Operation.ByTransfer] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference")
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      otherParty <- iban("other_party_country_code", "other_party_check_digits", "other_party_bban").?
    } yield {
      Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty)
    }
}
