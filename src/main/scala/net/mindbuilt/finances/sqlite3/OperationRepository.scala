package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.Helpers._
import net.mindbuilt.finances.application.OperationService
import net.mindbuilt.finances.application.OperationService.SearchCriterion
import net.mindbuilt.finances.business.Operation.{Breakdown, Id}
import net.mindbuilt.finances.business.{LocalInterval, Operation}
import net.mindbuilt.finances.sqlite3.OperationRepository._
import net.mindbuilt.finances.{IntToCents, business => port}

import java.sql.Connection
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.language.implicitConversions

class OperationRepository(implicit val database: EitherT[IO, Throwable, Database])
  extends port.OperationRepository
{
  override def getByInterval(interval: LocalInterval): EitherT[IO, Throwable, Seq[Operation]] =
    withConnection { implicit connection: Connection =>
      for {
        cardOperations <- getCardOperationsByInterval(interval)
        checkOperations <- getCheckOperationsByInterval(interval)
        debitOperations <- getDebitOperationsByInterval(interval)
        transferOperations <- getTransferOperationsByInterval(interval)
      } yield {
        cardOperations ++ checkOperations ++ debitOperations ++ transferOperations
      }
    }
    
  private[this] def getCardOperationsByInterval(interval: LocalInterval)(implicit connection: Connection): EitherT[IO, Throwable, Seq[Operation.ByCard]] =
    executeQueryWithEffect(
      """SELECT *
        |FROM "card_operation" "o"
        |INNER JOIN "card_breakdown" "b"
        |ON "o"."id" = "b"."operation"
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
      interval: _*
    )(cardOperationParser.*)
      .map(_
        .groupBy(_._1)
        .map(entry => entry._1 -> entry._2.map(_._2)).toSeq
        .map {
          case (Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, _), breakdown) => Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, breakdown)
        }
      )

  private[this] def getCheckOperationsByInterval(interval: LocalInterval)(implicit connection: Connection): EitherT[IO, Throwable, Seq[Operation.ByCheck]] =
    executeQueryWithEffect(
      """SELECT *
        |FROM "check_operation" "o"
        |INNER JOIN "check_breakdown" "b"
        |ON "o"."id" = "b"."operation"
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
      interval: _*
    )(checkOperationParser.*)
      .map(_
        .groupBy(_._1)
        .map(entry => entry._1 -> entry._2.map(_._2)).toSeq
        .map {
          case (Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, _), breakdown) => Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, breakdown)
        }
      )

  private[this] def getDebitOperationsByInterval(interval: LocalInterval)(implicit connection: Connection): EitherT[IO, Throwable, Seq[Operation.ByDebit]] =
    executeQueryWithEffect(
      """SELECT *
        |FROM "debit_operation" "o"
        |INNER JOIN "debit_breakdown" "b"
        |ON "o"."id" = "b"."operation"
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
      interval: _*
    )(debitOperationParser.*)
      .map(_
        .groupBy(_._1)
        .map(entry => entry._1 -> entry._2.map(_._2)).toSeq
        .map {
          case (Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, _), breakdown) => Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, breakdown)
        }
      )

  private[this] def getTransferOperationsByInterval(interval: LocalInterval)(implicit connection: Connection): EitherT[IO, Throwable, Seq[Operation.ByTransfer]] =
    executeQueryWithEffect(
      """SELECT *
        |FROM "transfer_operation" "o"
        |INNER JOIN "transfer_breakdown" "b"
        |ON "o"."id" = "b"."operation"
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
      interval: _*
    )(transferOperationParser.*)
      .map(_
        .groupBy(_._1)
        .map(entry => entry._1 -> entry._2.map(_._2)).toSeq
        .map {
          case (Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, _), breakdown) => Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, breakdown)
        }
      )

  override def search(criteria: Seq[_ <: OperationService.SearchCriterion]): EitherT[IO, Throwable, Seq[Operation]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect("""
        |SELECT *, "reference" AS "number" FROM "operation" "o"
        |INNER JOIN "breakdown" "b"
        |ON "o"."id"="b"."operation_id"
        |WHERE %s
        |""".stripMargin
        .format((criteria.map(_.clause) :+ "0=0")
          .map("(%s)".format(_))
          .mkString(" AND ")
        ),
        criteria.flatMap(_.namedParameters):_*
      )(operationsParser)
    }
  
  override def save(operation: Operation): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      (operation match {
        case cardOperation: Operation.ByCard => saveCardOperation(cardOperation)
        case checkOperation: Operation.ByCheck => saveCheckOperation(checkOperation)
        case debitOperation: Operation.ByDebit => saveDebitOperation(debitOperation)
        case transferOperation: Operation.ByTransfer => saveTransferOperation(transferOperation)
      })
        .orRollback
    }
  }

  override def save(operations: Seq[Operation]): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
        operations.map {
          case cardOperation: Operation.ByCard => saveCardOperation(cardOperation)
          case checkOperation: Operation.ByCheck => saveCheckOperation(checkOperation)
          case debitOperation: Operation.ByDebit => saveDebitOperation(debitOperation)
          case transferOperation: Operation.ByTransfer => saveTransferOperation(transferOperation)
        }
          .traverse
          .map(_.reduce((_, _) => ()))
          .orRollback
    }
  }

  override def updateBreakdowns(operation: Operation.Id, breakdowns: Seq[Breakdown]): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      (
        for {
          cardBreakdownDeleted <- executeUpdateWithEffect(
            """DELETE FROM
              |  "card_breakdown"
              |WHERE "operation"={operation}
              |""".stripMargin,
            "operation" -> operation
          )
          checkBreakdownDeleted <- executeUpdateWithEffect(
            """DELETE FROM
              |  "check_breakdown"
              |WHERE "operation"={operation}
              |""".stripMargin,
            "operation" -> operation
          )
          debitBreakdownDeleted <- executeUpdateWithEffect(
            """DELETE FROM
              |  "debit_breakdown"
              |WHERE "operation"={operation}
              |""".stripMargin,
            "operation" -> operation
          )
          transferBreakdownDeleted <- executeUpdateWithEffect(
            """DELETE FROM
              |  "transfer_breakdown"
              |WHERE "operation"={operation}
              |""".stripMargin,
            "operation" -> operation
          )
          method <- (cardBreakdownDeleted, checkBreakdownDeleted, debitBreakdownDeleted, transferBreakdownDeleted) match {
            case (0, 0, 0, 0) => EitherT.leftT[IO, Unit](new NoSuchElementException("No breakdown found for operation %s".format(operation)))
            case (_, 0, 0, 0) => EitherT.pure[IO, Throwable]("card")
            case (0, _, 0, 0) => EitherT.pure[IO, Throwable]("check")
            case (0, 0, _, 0) => EitherT.pure[IO, Throwable]("debit")
            case (0, 0, 0, _) => EitherT.pure[IO, Throwable]("transfer")
            case _ => EitherT.leftT[IO, Unit](new IllegalStateException("Breakdown found in multiple tables for operation %s, check the database state for data corruption".format(operation)))
          }
          query =
            """INSERT INTO "%s_breakdown"("operation", "credit", "category", "comment", "supplier")
              |VALUES({operation}, {credit}, {category}, {comment}, {supplier})
              |""".stripMargin.format(method)
          _ <- executeBatchWithEffect(
            query,
            breakdownToNamedParameters(operation, breakdowns.head),
            breakdowns.tail.map(breakdownToNamedParameters(operation, _)): _*
          )
        } yield ()
      )
        .orRollback
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
      _ <- executeBatchWithEffect(
        """INSERT INTO "card_breakdown"("operation", "credit", "category", "comment", "supplier")
          |VALUES({operation}, {credit}, {category}, {comment}, {supplier})
          |""".stripMargin,
        breakdownToNamedParameters(operation.id, operation.breakdown.head),
        operation.breakdown.tail.map(breakdownToNamedParameters(operation.id, _)):_*
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
      _ <- executeBatchWithEffect(
        """INSERT INTO "check_breakdown"("operation", "credit", "category", "comment", "supplier")
          |VALUES({operation}, {credit}, {category}, {comment}, {supplier})
          |""".stripMargin,
        breakdownToNamedParameters(operation.id, operation.breakdown.head),
        operation.breakdown.tail.map(breakdownToNamedParameters(operation.id, _)): _*
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
      _ <- executeBatchWithEffect(
        """INSERT INTO "debit_breakdown"("operation", "credit", "category", "comment", "supplier")
          |VALUES({operation}, {credit}, {category}, {comment}, {supplier})
          |""".stripMargin,
        breakdownToNamedParameters(operation.id, operation.breakdown.head),
        operation.breakdown.tail.map(breakdownToNamedParameters(operation.id, _)): _*
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
      _ <- executeBatchWithEffect(
        """INSERT INTO "transfer_breakdown"("operation", "credit", "category", "comment", "supplier")
          |VALUES({operation}, {credit}, {category}, {comment}, {supplier})
          |""".stripMargin,
        breakdownToNamedParameters(operation.id, operation.breakdown.head),
        operation.breakdown.tail.map(breakdownToNamedParameters(operation.id, _)): _*
      )
    } yield ()

  override def getById(id: Id): EitherT[IO, Throwable, Option[Operation]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT *, "reference" AS "number" FROM "operation"
          |INNER JOIN "breakdown"
          |ON "operation"."type"="breakdown"."operation_type"
          |AND "operation"."id"="breakdown"."operation_id"
          |WHERE "operation"."id"={id}""".stripMargin,
        namedParameters(
          "id" -> id.toString
        ):_*
     )(operationsParser)
        .map(_.headOption)
    }

  override def getAllCategories: EitherT[IO, Throwable, Set[String]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect("""SELECT DISTINCT "category" FROM "breakdown" WHERE "category" IS NOT NULL""")(str("category").*).map(_.toSet)
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
      "number" -> operation.checkNumber,
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
    
  implicit def breakdownToNamedParameters(operation: Operation.Id, breakdown: Breakdown): Seq[NamedParameter] =
    namedParameters(
      "operation" -> operation.toString,
      "credit" -> breakdown.credit.value,
      "category" -> breakdown.category,
      "comment" -> breakdown.comment,
      "supplier" -> breakdown.supplier
    )

  private val operationParser: RowParser[(Operation, Breakdown)] =
    str("type")
      .flatMap {
        case "card" => cardOperationParser
        case "check" => checkOperationParser
        case "debit" => debitOperationParser
        case "transfer" => transferOperationParser
      }
      
  private val operationsParser: ResultSetParser[Seq[Operation]] =
    operationParser.*
      .map(_
        .groupBy(_._1)
        .map(entry => entry._1 -> entry._2.map(_._2)).toSeq
        .map { entry: (Operation, List[Breakdown]) => entry match {
          case (Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, _), breakdown) =>
            Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, breakdown)
          case (Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, _), breakdown) =>
            Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, breakdown)
          case (Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, _), breakdown) =>
            Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, breakdown)
          case (Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, _), breakdown) =>
            Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, breakdown)
        }}
      )
      
  private val cardOperationParser: RowParser[(Operation.ByCard, Breakdown)] =
    for {
      id <- uuid("id")
      label <- str("label")
      card <- str("card")
      reference <- str("reference").?
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      credit <- int("credit").map(_.cents)
      category <- str("category").?
      comment <- str("comment").?
      supplier <- uuid("supplier").?
    } yield {
      Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, Seq.empty[Breakdown]) -> Breakdown(credit, category, comment, supplier)
    }
    
  private val checkOperationParser: RowParser[(Operation.ByCheck, Breakdown)] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      number <- str("number")
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      credit <- int("credit").map(_.cents)
      category <- str("category").?
      comment <- str("comment").?
      supplier <- uuid("supplier").?
    } yield {
      Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, Seq.empty[Breakdown]) -> Breakdown(credit, category, comment, supplier)
    }

  private val debitOperationParser: RowParser[(Operation.ByDebit, Breakdown)] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference").?
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      credit <- int("credit").map(_.cents)
      category <- str("category").?
      comment <- str("comment").?
      supplier <- uuid("supplier").?
    } yield {
      Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, Seq.empty[Breakdown]) -> Breakdown(credit, category, comment, supplier)
    }

  private val transferOperationParser: RowParser[(Operation.ByTransfer, Breakdown)] =
    for {
      id <- uuid("id")
      account <- iban("account_country_code", "account_check_digits", "account_bban")
      reference <- str("reference").?
      label <- str("label")
      operationDate <- localDate("operation_date")
      valueDate <- localDate("value_date")
      accountDate <- localDate("account_date")
      otherParty <- iban("other_party_country_code", "other_party_check_digits", "other_party_bban").?
      credit <- int("credit").map(_.cents)
      category <- str("category").?
      comment <- str("comment").?
      supplier <- uuid("supplier").?
    } yield {
      Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, Seq.empty[Breakdown]) -> Breakdown(credit, category, comment, supplier)
    }
    
  trait WhereClauseWriter[C <: SearchCriterion] {
    def namedParameters(criterion: C): Seq[NamedParameter]
    def clause(criterion: C): String
  }
  
  implicit class SearchCriterionWriteableAsWhereClause(criterion: SearchCriterion) {
    def clause: String = criterion match {
      case iban: SearchCriterion.Account => AccountWhereClauseWriter.clause(iban)
      case from: SearchCriterion.From => FromWhereClauseWriter.clause(from)
      case to: SearchCriterion.To => ToWhereClauseWriter.clause(to)
    }
    
    def namedParameters: Seq[NamedParameter] = criterion match {
      case iban: SearchCriterion.Account => AccountWhereClauseWriter.namedParameters(iban)
      case from: SearchCriterion.From => FromWhereClauseWriter.namedParameters(from)
      case to: SearchCriterion.To => ToWhereClauseWriter.namedParameters(to)
    }
  }
    
  implicit object AccountWhereClauseWriter extends WhereClauseWriter[SearchCriterion.Account] {
    override def namedParameters(criterion: SearchCriterion.Account): Seq[NamedParameter] = Seq(
      "account_country_code" -> criterion.iban.countryCode,
      "account_check_digits" -> criterion.iban.checkDigits,
      "account_bban" -> criterion.iban.bban
    )

    override def clause(criterion: SearchCriterion.Account): String =
      """"o"."account_country_code" = {account_country_code} AND "o"."account_check_digits" = {account_check_digits} AND "o"."account_bban" = {account_bban}"""
  }
  
  implicit object FromWhereClauseWriter extends WhereClauseWriter[SearchCriterion.From] {
    override def namedParameters(criterion: SearchCriterion.From): Seq[NamedParameter] = Seq(
      "from" -> ISO_LOCAL_DATE.format(criterion.date)
    )

    override def clause(criterion: SearchCriterion.From): String =
      """"o"."value_date" >= {from} OR "o"."account_date" >= {from} OR "o"."operation_date" >= {from}"""
  }
  
  implicit object ToWhereClauseWriter extends WhereClauseWriter[SearchCriterion.To] {
    override def namedParameters(criterion: SearchCriterion.To): Seq[NamedParameter] = Seq(
      "to" -> ISO_LOCAL_DATE.format(criterion.date)
    )
    
    override def clause(criterion: SearchCriterion.To): String =
      """"o"."value_date" <= {to} OR "o"."account_date" <= {to} OR "o"."operation_date" <= {to}"""
  }
}
