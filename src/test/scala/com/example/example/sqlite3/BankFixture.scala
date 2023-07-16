package com.example.example.sqlite3

import anorm.{BatchSql, NamedParameter}
import com.example.example.business.Bank
import com.example.example.sqlite3.BankFixture._

import java.sql.Connection
import scala.language.implicitConversions

trait BankFixture { self: InMemoryDatabase =>
  def withBanks[T](firstBank: Bank, otherBanks: Bank*)(test: Connection => T)(implicit connection: Connection): T = {
    BatchSql(
      "INSERT INTO `bank`(`bic`, `designation`) VALUES ({bic}, {designation})",
      firstBank,
      otherBanks.map(bankToNamedParameters):_*
    )
      .execute()
    test(connection)
  }
}

object BankFixture {
  implicit def bankToNamedParameters(bank: Bank): Seq[NamedParameter] =
    Seq(
      "bic" -> bank.bic.toString,
      "designation" -> bank.designation
    )
}