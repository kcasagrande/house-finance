package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Card, Iban}
import net.mindbuilt.finances.sqlite3.CardRepository.cardParser
import net.mindbuilt.finances.{business => port}

import java.sql.Connection
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.Try

class CardRepository(implicit val connection: Connection)
  extends port.CardRepository
{
  override def getByNumber(number: Card.Number): EitherT[IO, Throwable, Option[Card]] =
    EitherT(IO.delay {
      SQL("""SELECT * FROM card WHERE number={number}""")
        .on("number" -> number)
        .executeQuery()
        .as[Option[Try[Card]]](cardParser.singleOpt)
    })
}

object CardRepository {
  private def uuid(columnName: String): RowParser[UUID] = str(columnName).map(UUID.fromString)
  private def yearMonth(columnName: String): RowParser[YearMonth] = str(columnName).map(DateTimeFormatter.ofPattern("uuuu-MM").parse(_)).map(YearMonth.from)
  
  val cardParser: RowParser[Try[Card]] = for {
    number <- str("number")
    account_country_code <- str("account_country_code")
    account_check_digits <- str("account_check_digits")
    account_bban <- str("account_bban")
    holder <- uuid("holder")
    expiration <- yearMonth("expiration")
    _type <- str("type")
  } yield {
    Iban(account_country_code, account_check_digits, account_bban)
      .map { account =>
        Card(number, account, holder, expiration, _type)
      }
  }
}
