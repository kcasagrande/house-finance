package net.mindbuilt.finances

import anorm.SqlParser._
import anorm.{BatchSql, NamedParameter, ParameterValue, ResultSetParser, Row, RowParser, SQL, SqlRequestError, SqlResult}
import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import net.mindbuilt.finances.business.{Bank, Bic, Holder, Iban}
import org.sqlite.SQLiteConfig

import java.sql.Connection
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.{LocalDate, YearMonth}
import java.util.UUID
import scala.language.implicitConversions
import scala.util._

package object sqlite3
{
  implicit def localDateToString(localDate: LocalDate): String =
    ISO_LOCAL_DATE.format(localDate)
    
  implicit def tryToSqlResult[T](tryT: Try[T]): SqlResult[T] =
    tryT match {
      case Success(value) => anorm.Success(value)
      case Failure(throwable) => anorm.Error(SqlRequestError(throwable))
    }
    
  implicit def optionTryToTryOption[T](value: Option[Try[T]]): Try[Option[T]] = value match {
    case Some(Success(content)) => Success(Some(content))
    case Some(Failure(throwable)) => Failure(throwable)
    case None => Success(None)
  }
  
  implicit def liftEitherT[A, B](effect: IO[Either[A, B]]): EitherT[IO, A, B] = EitherT(effect)
  implicit def unliftEitherT[A, B](eitherT: EitherT[IO, A, B]): IO[Either[A, B]] = eitherT.value
  
  def withConnection[T](run: Connection => EitherT[IO, Throwable, T])(implicit database: EitherT[IO, Throwable, Database]): EitherT[IO, Throwable, T] = {
    val config = new SQLiteConfig()
    config.enableLoadExtension(true)
    Resource.make(
      database
        .map(_.url)
        .map(config.createConnection)
        .flatMap { implicit connection =>
          EitherT(IO.blocking(SQL("SELECT load_extension('/home/orion/finances/uuid')").executeQuery().asTry[Unit](RowParser.successful.single.map(_ => ())).toEither))
            .map(_ => connection)
        }
        .map { connection =>
          connection.setAutoCommit(false)
          connection
        }
  )(connection => EitherT.liftF(IO.blocking(connection.close())))
      .use(run)
  }
  
  def executeQueryWithEffect[T](query: String, namedParameters: NamedParameter*)(parser: ResultSetParser[T])(implicit connection: Connection): EitherT[IO, Throwable, T] =
    EitherT(IO.blocking(SQL(query).on(namedParameters:_*).executeQuery().asTry[T](parser).toEither))

  def executeWithEffect(sql: String, namedParameters: NamedParameter*)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    EitherT(IO.blocking(Try { SQL(sql).on(namedParameters: _*).execute() }.toEither.map(_ => ())))
    
  def executeBatchWithEffect(sql: String, firstNamedParameters: Seq[NamedParameter], otherNamedParameters: Seq[NamedParameter]*)(implicit connection: Connection): EitherT[IO, Throwable, Array[Int]] =
    EitherT(IO.blocking(Try { BatchSql(sql, firstNamedParameters, otherNamedParameters:_*).execute() }.toEither))

  def uuid(columnName: String): RowParser[UUID] = str(columnName).map(UUID.fromString)
  
  def bic(columnName: String): RowParser[Bic] = (row: Row) =>
    for {
      institutionCode <- str(columnName).map(_.substring(0, 4))(row)
      countryCode <- str(columnName).map(_.substring(4, 6))(row)
      locationCode <- str(columnName).map(_.substring(6, 8))(row)
      branchCode <- str(columnName).map(_.substring(8))(row)
      bic <- tryToSqlResult(Bic(institutionCode, countryCode, locationCode, branchCode))
    } yield {
      bic
    }

  def yearMonth(columnName: String): RowParser[YearMonth] = str(columnName).map(DateTimeFormatter.ofPattern("uuuu-MM").parse(_)).map(YearMonth.from)

  def localDate(columnName: String): RowParser[LocalDate] = (row: Row) => str(columnName).apply(row)
    .flatMap { string: String =>
      Try {
        LocalDate.from(ISO_LOCAL_DATE.parse(string))
      }
    }
  
  def iban(countryCodeColumnName: String, checkDigitsColumnName: String, bbanColumnName: String): RowParser[Iban] = (row: Row) =>
    for {
      countryCode <- str(countryCodeColumnName).apply(row)
      checkDigits <- str(checkDigitsColumnName).apply(row)
      bban <- str(bbanColumnName).apply(row)
      iban <- tryToSqlResult(Iban(countryCode, checkDigits, bban))
    } yield {
      iban
    }
  def namedParameters(parameters: (String, ParameterValue)*): Seq[NamedParameter] = parameters.map(tuple => NamedParameter(tuple._1, tuple._2))
  
  implicit def bicToNamedParameters(bic: Bic): Seq[NamedParameter] = namedParameters(
    "bic" -> bic.toString()
  )

  implicit def bankToNamedParameters(bank: Bank): Seq[NamedParameter] = namedParameters(
    "bic" -> bank.bic.toString(),
    "designation" -> bank.designation
  )

  implicit def holderToNamedParameters(holder: Holder.Single): Seq[NamedParameter] = namedParameters(
    "id" -> holder.id.toString,
    "name" -> holder.name
  )
  
  implicit def optionTryToEither[T](optionTry: Option[Try[T]]): Either[Throwable, Option[T]] =
    optionTry.map(_.map(Some(_))).getOrElse(Success(None)).toEither
    
  implicit class ExtendedRowParser[A](rowParser: RowParser[A]) {
    def set: ResultSetParser[Set[A]] = rowParser.*.map(_.toSet)
  }
  
  implicit class ExtendedEitherT[T](eitherT: EitherT[IO, Throwable, T]) {
    def orRollback(implicit connection: Connection): EitherT[IO, Throwable, T] =
      eitherT
        .semiflatTap { _ =>
          IO(connection.commit())
        }
        .leftSemiflatTap { _ =>
          IO(connection.rollback())
        }
  }
}
