package net.mindbuilt.finances

import anorm.SqlParser._
import anorm.{BatchSql, NamedParameter, ParameterValue, Row, RowParser, SQL, SqlQueryResult, SqlRequestError, SqlResult}
import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import net.mindbuilt.finances.business.{Bank, Bic, Holder, Iban}

import java.sql.{Connection, DriverManager}
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
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
  
  def withConnection[T](run: Connection => IO[T])(implicit database: Database): IO[T] =
    Resource.make(IO.delay(DriverManager.getConnection(database.url)))(connection => IO.delay(connection.close()))
      .use(run)
  
  def executeQueryWithEffect(query: String, namedParameters: NamedParameter*)(implicit connection: Connection): EitherT[IO, Throwable, SqlQueryResult] =
    EitherT(IO.delay(Try { SQL(query).on(namedParameters:_*).executeQuery() }.toEither))

  def executeWithEffect(sql: String, namedParameters: NamedParameter*)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    EitherT(IO.delay(Try { SQL(sql).on(namedParameters: _*).execute() }.toEither.map(_ => ())))
    
  def executeBatchWithEffect(sql: String, firstNamedParameters: Seq[NamedParameter], otherNamedParameters: Seq[NamedParameter]*)(implicit connection: Connection): EitherT[IO, Throwable, Array[Int]] =
    EitherT(IO.delay(Try { BatchSql(sql, firstNamedParameters, otherNamedParameters:_*).execute() }.toEither))

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
}
