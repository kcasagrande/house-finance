package net.mindbuilt.finances.sqlite3

import anorm.NamedParameter
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Card
import net.mindbuilt.finances.sqlite3.CardFixture._

import java.sql.Connection
import scala.language.implicitConversions

trait CardFixture { self: InMemoryDatabase =>
  def withCards[T](firstCard: Card, otherCards: Card*)
    (test: EitherT[IO, Throwable, Database] => IO[T])
    (implicit database: EitherT[IO, Throwable, Database]): IO[T] = {
    withConnection { implicit connection: Connection =>
      executeBatchWithEffect(
        """INSERT INTO `card`(`number`, `account_country_code`, `account_check_digits`, `account_bban`, `holder`, `expiration`, `type`)
          |VALUES ({number}, {account_country_code}, {account_check_digits}, {account_bban}, {holder}, {expiration}, {type})"""
          .stripMargin,
        cardToNamedParameters(firstCard),
        otherCards.map(cardToNamedParameters): _*
      )
    }
      .value
      .flatMap(_ => test(database))
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
      "type" -> card.`type`
    )
}