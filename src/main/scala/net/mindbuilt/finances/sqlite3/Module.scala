package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import com.softwaremill.macwire.wire
import net.mindbuilt.finances.{Configuration, business}

trait Module
{
  def configuration: EitherT[IO, Throwable, Configuration]
  implicit lazy val database: EitherT[IO, Throwable, Database] = configuration
    .map(_.database)
    .map("jdbc:sqlite:file:" + _ + "?mode=ro")
    .map(Database)
  lazy val bankRepository: business.BankRepository = wire[BankRepository]
  lazy val accountRepository: business.AccountRepository = wire[AccountRepository]
  lazy val cardRepository: business.CardRepository = wire[CardRepository]
  lazy val operationRepository: business.OperationRepository = wire[OperationRepository]
}
