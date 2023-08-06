package net.mindbuilt.finances.sqlite3

import anorm.{BatchSql, NamedParameter}
import net.mindbuilt.finances.business.Card
import net.mindbuilt.finances.sqlite3.CardFixture._

import java.sql.Connection
import scala.language.implicitConversions

trait CardFixture { self: InMemoryDatabase =>
  def withCards[T](firstCard: Card, otherCards: Card*)(test: Connection => T)(implicit connection: Connection): T = {
    BatchSql(
      """INSERT INTO `card`(`number`, `account_country_code`, `account_check_digits`, `account_bban`, `holder`, `expiration`, `type`)
        |VALUES ({number}, {account_country_code}, {account_check_digits}, {account_bban}, {holder}, {expiration}, {type})"""
        .stripMargin,
      firstCard,
      otherCards.map(cardToNamedParameters):_*
    )
      .execute()
    test(connection)
  }
}

object CardFixture {
  implicit def cardToNamedParameters(card: Card): Seq[NamedParameter] =
    Seq(
      "number" -> card.number,
      "account_country_code" -> card.account.countryCode,
      "account_check_digits" -> card.account.checkDigits,
      "account_bban" -> card.account.bban,
      "holder" -> card.holder.toString,
      "expiration" -> "%04d-%02d".format(card.expiration.getYear, card.expiration.getMonthValue),
      "type" -> card.`type`.toString
    )
}