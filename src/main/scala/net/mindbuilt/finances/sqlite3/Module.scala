package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import com.softwaremill.macwire.wire
import net.mindbuilt.finances.business

trait Module
{
  implicit def database: EitherT[IO, Throwable, Database]
  lazy val bankRepository: business.BankRepository = wire[BankRepository]
  lazy val accountRepository: business.AccountRepository = wire[AccountRepository]
  lazy val cardRepository: business.CardRepository = wire[CardRepository]
  lazy val operationRepository: business.OperationRepository = wire[OperationRepository]
}
