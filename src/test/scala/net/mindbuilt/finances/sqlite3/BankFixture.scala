package net.mindbuilt.finances.sqlite3

import anorm.NamedParameter
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Bank
import net.mindbuilt.finances.sqlite3.AccountFixture._

import java.sql.Connection
import scala.language.implicitConversions

trait BankFixture
{ self: InMemoryDatabase =>
  def withBanks[T](firstBank: Bank, otherBanks: Bank*)
    (test: EitherT[IO, Throwable, Database] => IO[T])
    (implicit database: EitherT[IO, Throwable, Database]): IO[T] =
    withConnection { implicit connection: Connection =>
      executeBatchWithEffect(
        """INSERT INTO "bank"("bic", "designation") VALUES ({bic}, {designation})""",
        bankToNamedParameters(firstBank),
        otherBanks.map(bankToNamedParameters): _*
      )
    }
      .value
      .flatMap(_ => test(database))
}

object BankFixture {
  implicit def bankToNamedParameters(bank: Bank): Seq[NamedParameter] =
    Seq(
      "bic" -> bank.bic.toString,
      "designation" -> bank.designation
    )
}