package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Card
import net.mindbuilt.finances.sqlite3.CardRepository._
import net.mindbuilt.finances.{business => port}

import java.sql.Connection
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.language.implicitConversions

class CardRepository(implicit val database: EitherT[IO, Throwable, Database])
  extends port.CardRepository
{
  override def getAll: EitherT[IO, Throwable, Set[Card]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "number",
          |  "account_country_code",
          |  "account_check_digits",
          |  "account_bban",
          |  "holder",
          |  "expiration",
          |  "type"
          |FROM "card"
          |""".stripMargin
      )(cardParser.set)
    }

  override def getByNumber(number: Card.Number): EitherT[IO, Throwable, Option[Card]] =
    withConnection { implicit connection: Connection =>
    executeQueryWithEffect(
      """SELECT
        |  "number",
        |  "account_country_code",
        |  "account_check_digits",
        |  "account_bban",
        |  "holder",
        |  "expiration",
        |  "type"
        |FROM "card"
        |WHERE "number"={number}
        |""".stripMargin,
      namedParameters(
        ("number" -> number)
      ):_*
    )(cardParser.singleOpt)
  }

  override def save(card: Card): EitherT[IO, Throwable, Unit] =
    withConnection { implicit connection: Connection =>
      executeWithEffect(
        """INSERT INTO "card"(
          |  "number",
          |  "account_country_code",
          |  "account_check_digits",
          |  "account_bban",
          |  "holder",
          |  "expiration",
          |  "type"
          |) VALUES (
          |  {number},
          |  {account_country_code},
          |  {account_check_digits},
          |  {account_bban},
          |  {holder},
          |  {expiration},
          |  {type}
          |)""".stripMargin,
        card:_*
      )
    }
}

object CardRepository {
  private def uuid(columnName: String): RowParser[UUID] = str(columnName).map(UUID.fromString)
  private def yearMonth(columnName: String): RowParser[YearMonth] = str(columnName).map(DateTimeFormatter.ofPattern("uuuu-MM").parse(_)).map(YearMonth.from)
  
  implicit def cardToNamedParameters(card: Card): Seq[NamedParameter] =
    namedParameters(
      "number" -> card.number,
      "account_country_code" -> card.account.countryCode,
      "account_check_digits" -> card.account.checkDigits,
      "account_bban" -> card.account.bban,
      "holder" -> card.holder.toString,
      "expiration" -> DateTimeFormatter.ofPattern("uuuu-MM").format(card.expiration),
      "type" -> card.`type`
    )
  
  val cardParser: RowParser[Card] = for {
    number <- str("number")
    account <- iban("account_country_code", "account_check_digits", "account_bban")
    holder <- uuid("holder")
    expiration <- yearMonth("expiration")
    _type <- str("type")
  } yield {
    Card(number, account, holder, expiration, _type)
  }
}
