package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Card, Holder, Iban}
import net.mindbuilt.finances.sqlite3.CardRepository._
import net.mindbuilt.finances.{business => port}

import java.sql.Connection
import java.time.format.DateTimeFormatter
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

  override def getByAccount(account: Iban): EitherT[IO, Throwable, Set[Card]] =
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
          |WHERE
          |  "account_country_code"={accountCountryCode}
          |AND
          |  "account_check_digits"={accountCheckDigits}
          |AND
          |  "account_bban"={accountBban}
          |""".stripMargin,
        namedParameters(
          "accountCountryCode" -> account.countryCode,
          "accountCheckDigits" -> account.checkDigits,
          "accountBban" -> account.bban
        ):_*
      )(cardParser.set)
    }

  override def getByAccountWithHolder(account: Iban): EitherT[IO, Throwable, Set[(Card, Holder.Single)]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "c"."number" AS "number",
          |  "c"."account_country_code" AS "account_country_code",
          |  "c"."account_check_digits" AS "account_check_digits",
          |  "c"."account_bban" AS "account_bban",
          |  "h"."id" AS "holderId",
          |  "h"."name" AS "holderName",
          |  "c"."expiration" AS "expiration",
          |  "c"."type" AS "type"
          |FROM "card" "c"
          |INNER JOIN "holder" "h"
          |ON "c"."holder"="h"."id"
          |WHERE
          |  "c"."account_country_code"={accountCountryCode}
          |AND
          |  "c"."account_check_digits"={accountCheckDigits}
          |AND
          |  "c"."account_bban"={accountBban}
          |""".stripMargin,
        namedParameters(
          "accountCountryCode" -> account.countryCode,
          "accountCheckDigits" -> account.checkDigits,
          "accountBban" -> account.bban
        ):_*
      )(cardWithHolderParser.set)
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
        .orRollback
    }
}

object CardRepository {
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
  
  private val cardParser: RowParser[Card] = for {
    number <- str("number")
    account <- iban("account_country_code", "account_check_digits", "account_bban")
    holder <- uuid("holder")
    expiration <- yearMonth("expiration")
    _type <- str("type")
  } yield {
    Card(number, account, holder, expiration, _type)
  }
  
  private val cardWithHolderParser: RowParser[(Card, Holder.Single)] = for {
    number <- str("number")
    account <- iban("account_country_code", "account_check_digits", "account_bban")
    holderId <- uuid("holderId")
    holderName <- str("holderName")
    expiration <- yearMonth("expiration")
    _type <- str("type")
  } yield {
    (Card(number, account, holderId, expiration, _type), Holder.Single(holderId, holderName))
  }
}
