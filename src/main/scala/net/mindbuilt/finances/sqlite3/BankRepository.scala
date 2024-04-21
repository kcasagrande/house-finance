package net.mindbuilt.finances.sqlite3

import anorm.RowParser
import anorm.SqlParser._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Bank, Bic, BankRepository => Port}
import net.mindbuilt.finances.sqlite3.BankRepository._

import java.sql.Connection
import scala.language.implicitConversions

class BankRepository(implicit database: EitherT[IO, Throwable, Database])
  extends Port
{
  override def getAll: EitherT[IO, Throwable, Set[Bank]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "bic",
          |  "designation"
          |FROM "bank"""".stripMargin
      )(bank.set)
    }
  
  override def getByBic(bic: Bic): EitherT[IO, Throwable, Option[Bank]] = {
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "bic",
          |  "designation"
          |FROM "bank"
          |WHERE "bic"={bic}""".stripMargin,
        bic: _*
      )(bank.singleOpt)
    }
  }

  override def save(bank: Bank): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      executeWithEffect(
        """INSERT INTO "bank"("bic", "designation")
          |VALUES ({bic}, {designation})""".stripMargin,
        bank: _*
      )
        .orRollback
    }
  }
}

object BankRepository {
  implicit val bank: RowParser[Bank] =
    for {
      bic <- bic("bic")
      designation <- str("designation")
    } yield {
      Bank(bic, designation)
    }
}