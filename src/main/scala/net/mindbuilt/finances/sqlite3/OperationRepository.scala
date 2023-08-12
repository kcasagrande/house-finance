package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Operation.Id
import net.mindbuilt.finances.business.{LocalInterval, Operation}
import net.mindbuilt.finances.sqlite3.OperationRepository._
import net.mindbuilt.finances.{IntToCents, business => port}

import java.sql.Connection
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.language.implicitConversions

class OperationRepository(implicit val database: Database)
  extends port.OperationRepository
{
  override def getByInterval(interval: LocalInterval): EitherT[IO, Throwable, Seq[Operation]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT * FROM "operation"
          |WHERE (
          |"operation_date" >= {start}
          |OR "value_date" >= {start}
          |OR "account_date" >= {start}
          |) AND (
          |"operation_date" <= {end}
          |OR "value_date" <= {end}
          |OR "account_date" <= {end}
          |)
          |ORDER BY "operation_date" ASC, "value_date" ASC, "account_date" ASC
          |""".stripMargin,
        interval:_*
      )
        .map(_.as(operationParser.*))
    }

  override def save(operation: Operation): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      operation match {
        case cardOperation: Operation.ByCard => saveCardOperation(cardOperation)
        case checkOperation: Operation.ByCheck => saveCheckOperation(checkOperation)
        case debitOperation: Operation.ByDebit => saveDebitOperation(debitOperation)
        case transferOperation: Operation.ByTransfer => saveTransferOperation(transferOperation)
      }
    }
  }

  private[this] def saveCardOperation(operation: Operation.ByCard)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    for {
      _ <- executeWithEffect(
        """INSERT INTO "card_operation"("id", "card", "reference", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {card}, {reference}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin,
        operation:_*
      )
      _ <- executeWithEffect(
        """INSERT INTO "card_breakdown"("operation", "credit")
          |VALUES({id}, {credit})
          |""".stripMargin,
        operation:_*
      )
    } yield ()

  private[this] def saveCheckOperation(operation: Operation.ByCheck)(implicit connection: Connection): EitherT[IO, Throwable, Unit] = {
    for {
      _ <- executeWithEffect(
        """INSERT INTO "check_operation"("id", "account_country_code", "account_check_digits", "account_bban", "number", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {number}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin,
        checkOperationToNamedParameters(operation): _*
      )
      _ <- executeWithEffect(
        """INSERT INTO "check_breakdown"("operation", "credit")
          |VALUES({id}, {credit})
          |""".stripMargin,
        checkOperationToNamedParameters(operation):_*
      )
    } yield ()
  }

  private[this] def saveDebitOperation(operation: Operation.ByDebit)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    for {
      _ <- executeWithEffect(
        """INSERT INTO "debit_operation"("id", "account_country_code", "account_check_digits", "account_bban", "reference", "label", "operation_date", "value_date", "account_date")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {reference}, {label}, {operation_date}, {value_date}, {account_date})
          |""".stripMargin,
        operation: _*
      )
      _ <- executeWithEffect(
        """INSERT INTO "debit_breakdown"("operation", "credit")
          |VALUES({id}, {credit})
          |""".stripMargin,
        operation: _*
      )
    } yield ()

  private[this] def saveTransferOperation(operation: Operation.ByTransfer)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    for {
      _ <- executeWithEffect(
        """INSERT INTO "transfer_operation"("id", "account_country_code", "account_check_digits", "account_bban", "reference", "label", "operation_date", "value_date", "account_date", "other_party_country_code", "other_party_check_digits", "other_party_bban")
          |VALUES ({id}, {account_country_code}, {account_check_digits}, {account_bban}, {reference}, {label}, {operation_date}, {value_date}, {account_date}, {other_party_country_code}, {other_party_check_digits}, {other_party_bban})
          |""".stripMargin,
        operation: _*
      )
      _ <- executeWithEffect(
        """INSERT INTO "transfer_breakdown"("operation", "credit")
          |VALUES({id}, {credit})
          |""".stripMargin,
        operation: _*
      )
    } yield ()

  override def getById(id: Id): EitherT[IO, Throwable, Option[Operation]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT * FROM "operation"
          |WHERE "id"={id}""".stripMargin,
        namedParameters(
          "id" -> id.toString
        ):_*
     )
        .map(_.as(operationParser.singleOpt))
    }

  }

object OperationRepository {
  implicit def intervalToNamedParameters(interval: LocalInterval): Seq[NamedParameter] =
    namedParameters(
      "start" -> ISO_LOCAL_DATE.format(interval.start),
      "end" -> ISO_LOCAL_DATE.format(interval.end)
    )
    
  implicit def cardOperationToNamedParameters(operation: Operation.ByCard): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "card" -> operation.card,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "credit" -> operation.credit.value,
      "operation_date" -> ISO_LOCAL_DATE.format(operation.operationDate),
      "value_date" -> ISO_LOCAL_DATE.format(operation.valueDate),
      "account_date" -> ISO_LOCAL_DATE.format(operation.accountDate)
    )

  implicit def checkOperationToNamedParameters(operation: Operation.ByCheck): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "number" -> operation.number,
      "label" -> operation.label,
      "credit" -> operation.credit.value,
      "operation_date" -> ISO_LOCAL_DATE.format(operation.operationDate),
      "value_date" -> ISO_LOCAL_DATE.format(operation.valueDate),
      "account_date" -> ISO_LOCAL_DATE.format(operation.accountDate)
    )

  implicit def debitOperationToNamedParameters(operation: Operation.ByDebit): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "credit" -> operation.credit.value,
      "operation_date" -> ISO_LOCAL_DATE.format(operation.operationDate),
      "value_date" -> ISO_LOCAL_DATE.format(operation.valueDate),
      "account_date" -> ISO_LOCAL_DATE.format(operation.accountDate)
    )

  implicit def transferOperationToNamedParameters(operation: Operation.ByTransfer): Seq[NamedParameter] =
    namedParameters(
      "id" -> operation.id.toString,
      "account_country_code" -> operation.account.countryCode,
      "account_check_digits" -> operation.account.checkDigits,
      "account_bban" -> operation.account.bban,
      "reference" -> operation.reference,
      "label" -> operation.label,
      "credit" -> operation.credit.value,
      "operation_date" -> ISO_LOCAL_DATE.format(operation.operationDate),
      "value_date" -> ISO_LOCAL_DATE.format(operation.valueDate),
      "account_date" -> ISO_LOCAL_DATE.format(operation.accountDate),
      "other_party_country_code" -> operation.otherParty.map(_.countryCode),
      "other_party_check_digits" -> operation.otherParty.map(_.checkDigits),
      "other_party_bban" -> operation.otherParty.map(_.bban)
    )

  private val operationParser: RowParser[Operation] =
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
      credit <- int("credit").map(_.cents)
      card <- str("card")
      reference <- str("reference")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByCard(id, card, reference, label, credit, operationDate, valueDate, accountDate)
    }
    
  private val checkOperationParser: RowParser[Operation.ByCheck] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      number <- str("reference")
      label <- str("label")
      credit <- int("credit").map(_.cents)
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByCheck(id, account, number, label, credit, operationDate, valueDate, accountDate)
    }

  private val debitOperationParser: RowParser[Operation.ByDebit] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference")
      label <- str("label")
      credit <- int("credit").map(_.cents)
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
    } yield {
      Operation.ByDebit(id, account, reference, label, credit, operationDate, valueDate, accountDate)
    }

  private val transferOperationParser: RowParser[Operation.ByTransfer] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference")
      label <- str("label")
      credit <- int("credit").map(_.cents)
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      otherParty <- iban("other_party_country_code", "other_party_check_digits", "other_party_bban").?
    } yield {
      Operation.ByTransfer(id, account, reference, label, credit, operationDate, valueDate, accountDate, otherParty)
    }
}
