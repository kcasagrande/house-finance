package com.example.example.bank.sample

import cats.effect._
import com.example.example.{Cents, IntToCents}
import fs2._
import fs2.data.csv._
import fs2.data.text._
import fs2.io.file._
import fs2.text.lines

import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.language.postfixOps
import scala.util.Try


object CsvReader
  extends (Path => Stream[IO, Operation]) {
  override def apply(path: Path): Stream[IO, Operation] = Files[IO].readAll(path)
    .through[IO, String](fs2.text.decodeWithCharset(Charset.forName("ISO-8859-1"))(RaiseThrowable.fromApplicativeError))
    .through(lines)
    .drop(10L)
    .flatMap(line => Stream.iterable(line.toCharArray :+ '\n'))
    .through(decodeUsingHeaders[Operation](separator = ';')(F = RaiseThrowable.fromApplicativeError, C = CharLikeChunks.charStreamCharLike, T = OperationDecoder, H = ParseableHeader.StringParseableHeader))

  implicit object LocalDateDecoder extends CellDecoder[LocalDate] {
    override def apply(cell: String): DecoderResult[LocalDate] =
      CellDecoder.stringDecoder(cell)
        .flatMap(dateAsText =>
          Try {
            LocalDate.from(DateTimeFormatter.ofPattern("dd/MM/yyyy").parse(dateAsText))
          }
            .toEither
            .left
            .map(throwable => new DecoderError(throwable.getMessage, None, throwable))
        )
  }

  implicit object CentsDecoder extends CellDecoder[Cents] {
    override def apply(cell: String): DecoderResult[Cents] =
      CellDecoder.stringDecoder(cell)
        .flatMap(centsAsText =>
          Try {
            centsAsText.replaceAll("\\s", "")
              .split(',')
              .filterNot(_.isEmpty)
              .toList match {
              case Nil => 0 cents
              case euros :: cents :: Nil => (euros.toInt * 100 + cents.toInt) cents
              case euros :: Nil => (euros.toInt * 100) cents
              case anythingElse => throw new IllegalArgumentException(anythingElse.mkString(",") + " cannot be parsed to cents")
            }
          }
            .toEither
            .left
            .map(throwable => new DecoderError(throwable.getMessage, None, throwable))
        )
  }

  implicit object OperationDecoder
    extends CsvRowDecoder[Operation, String] {
    def apply(row: CsvRow[String]): DecoderResult[Operation] =
      for {
        date <- row.as[LocalDate]("Date")
        dateValeur <- row.as[LocalDate]("Date valeur")
        libellé <- row.as[String]("Libellé")
        débit <- row.as[Cents]("Débit euros")
        crédit <- row.as[Cents]("Crédit euros")
      } yield {
        Operation(date, dateValeur, libellé, débit, crédit)
      }
  }
}