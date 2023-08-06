package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm.{RowParser, SQL}
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Bank, Bic, BankRepository => Port}
import net.mindbuilt.finances.sqlite3.BankRepository._

import java.sql.Connection
import scala.language.implicitConversions
import scala.util._

class BankRepository(implicit database: Database)
  extends Port
{
  override def getByBic(bic: Bic): EitherT[IO, Throwable, Option[Bank]] = {
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "bic",
          |  "designation"
          |FROM "bank"
          |WHERE "bic"={bic}""".stripMargin,
        bic: _*
      )
        .subflatMap(_.as[Option[Try[Bank]]](bank.singleOpt).toEither)
    }
  }

  override def save(bank: Bank): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      executeWithEffect(
        """INSERT INTO "bank"("bic", "designation")
          |VALUES ({bic}, {designation})""".stripMargin,
        bank: _*
      )
    }
  }
}

object BankRepository {
  implicit val bank: RowParser[Try[Bank]] =
    for {
      bic <- bic("bic")
      designation <- str("designation")
    } yield {
      bic.map(Bank(_, designation))
    }
}