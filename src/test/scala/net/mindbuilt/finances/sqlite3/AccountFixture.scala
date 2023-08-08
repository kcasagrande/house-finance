package net.mindbuilt.finances.sqlite3

import anorm.NamedParameter
import cats.effect.IO
import net.mindbuilt.finances.business.Account
import net.mindbuilt.finances.sqlite3.AccountFixture._

import java.sql.Connection
import scala.language.implicitConversions

trait AccountFixture
{ self: InMemoryDatabase =>
  def withAccounts[T](firstAccount: Account, otherAccounts: Account*)
    (test: Database => IO[T])
    (implicit database: Database): IO[T] =
    withConnection { implicit connection: Connection =>
      executeBatchWithEffect(
        """INSERT INTO "account"("bank", "country_code", "check_digits", "bban", "domiciliation")
          |VALUES ({bank}, {country_code}, {check_digits}, {bban}, {domiciliation})""".stripMargin,
        accountToNamedParameters(firstAccount),
        otherAccounts.map(accountToNamedParameters): _*
      )
    }
      .flatMap(_ => test(database))
}

object AccountFixture {
  implicit def accountToNamedParameters(account: Account): Seq[NamedParameter] =
    namedParameters(
      "bank" -> account.bank.toString,
      "country_code" -> account.iban.countryCode,
      "check_digits" -> account.iban.checkDigits,
      "bban" -> account.iban.bban,
      "domiciliation" -> account.domiciliation
    )
}