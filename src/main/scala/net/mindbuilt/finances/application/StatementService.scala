package net.mindbuilt.finances.application

import cats.Monad
import cats.data.EitherT
import cats.effect.IO
import fs2.Stream
import fs2.data.csv._
import fs2.data.csv.lenient.attemptDecodeUsingHeaders
import fs2.io.file.{Files, Path}
import fs2.text.lines
import net.mindbuilt.finances.application.StatementService._
import net.mindbuilt.finances.business.{AccountRepository, Card, CardRepository, Iban, Operation, OperationRepository, Statement}
import net.mindbuilt.finances.{Cents, IntToCents}

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.{Locale, UUID}
import scala.annotation.nowarn
import scala.language.{implicitConversions, postfixOps}
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class StatementService(
  accountRepository: AccountRepository,
  operationRepository: OperationRepository,
  cardRepository: CardRepository
) {
  def `import`(account: Iban, content: Stream[IO, String]): EitherT[IO, Throwable, Int] = {
    val cardRules = Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get("src/main/resources/rules.d/card.txt"))).through(lines).map(_.r)
    for {
      cards <- cardRepository.getByAccount(account)
      operations <- EitherT(content
        .through(attemptDecodeUsingHeaders[Statement.Row](';'))
        .compile.toList
        .map(_.foldLeft(Either.right[MultipleCsvExceptions](List.empty[Statement.Row])) { (result, element) =>
          result match {
            case left@Left(exceptions) =>
              element match {
                case Left(exception) => Left(exceptions :+ exception)
                case Right(_) => left
              }
            case Right(rows) =>
              element match {
                case Left(exception) => Left(exception)
                case Right(row) => Either.right[MultipleCsvExceptions](rows :+ row)
              }
          }
        })
      )
        .flatMap(_.map { statementRow => statementRowToOperation(statementRow, cards.map(_.number), cardRules) }.traverse)
    } yield {
      println(operations.mkString("\n"))
      operations.length
    }
  }
}

object StatementService {

  implicit class ExtendedEither(either: Either.type) {
    def left[B]: ExtendedEither.LeftPartiallyApplied[B] = new ExtendedEither.LeftPartiallyApplied[B]

    def right[A]: ExtendedEither.RightPartiallyApplied[A] = new ExtendedEither.RightPartiallyApplied[A]
  }

  object ExtendedEither {
    class RightPartiallyApplied[A](
      @nowarn
      private val dummy: Boolean = true
    )
      extends AnyVal {
      def apply[B](value: B): Either[A, B] = Right[A, B](value)
    }

    class LeftPartiallyApplied[B](
      @nowarn
      private val dummy: Boolean = true
    )
      extends AnyVal {
      def apply[A](value: A): Either[A, B] = Left[A, B](value)
    }
  }

  case class MultipleCsvExceptions(csvExceptions: Seq[CsvException])
    extends Throwable {
    override def getMessage: String = csvExceptions.map(_.getMessage).mkString("\n")

    def :+(csvException: CsvException): MultipleCsvExceptions = this.copy(csvExceptions = this.csvExceptions :+ csvException)
  }

  implicit def csvExceptionToMultipleCsvExceptions(csvException: CsvException): MultipleCsvExceptions = MultipleCsvExceptions(Seq(csvException))

  implicit def tryToDecoderResult[T](tryT: Try[T]): DecoderResult[T] =
    tryT match {
      case Success(value) => Right(value)
      case Failure(throwable) => Left(new DecoderError(throwable.getMessage, None, throwable))
    }

  implicit val localDateDecoder: CellDecoder[LocalDate] = CellDecoder.stringDecoder
    .emap { dateAsString =>
      Try {
        LocalDate.from(
          DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .parse(dateAsString)
        )
      }
    }

  implicit val centsDecoder: CellDecoder[Cents] = CellDecoder.instance { cell =>
    Try(NumberFormat.getInstance(Locale.FRANCE).parse(cell).doubleValue())
      .map(_ * 100.0)
      .map(Math.floor)
      .map(_.toInt)
      .map(Cents)
  }

  implicit val statementRowDecoder: CsvRowDecoder[Statement.Row, String] = CsvRowDecoder.instance { row =>
    for {
      date <- row.as[LocalDate]("Date")
      dateValeur <- row.as[LocalDate]("Date valeur")
      libelle <- row.as[String]("Libellé").map(_.split('\n').mkString(" ").replaceAll(" +", " ").trim)
      debit <- row.asOptional[Cents]("Débit euros").map(_.getOrElse(0 euros))
      credit <- row.asOptional[Cents]("Crédit euros").map(_.getOrElse(0 euros))
    } yield {
      Statement.Row(date, dateValeur, libelle, debit, credit)
    }
  }

  def statementRowToOperation(
    statementRow: Statement.Row,
    cardsOfAccount: Set[Card.Number] = Set.empty[Card.Number],
    cardRules: Stream[IO, Regex]
  )(implicit operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()): EitherT[IO, Throwable, Operation] = {
    for {
      groups <- EitherT.liftF(cardRules.collectFirst { rule => statementRow.libelle match {
        case rule(cardSuffix, incompleteDate) => (cardSuffix, incompleteDate)
      }}.compile.toList)
        .subflatMap(_.headOption.toRight(new MatchError("No rule matching row label \"%s\".".format(statementRow.libelle))))
      card <- EitherT.fromOption[IO](cardsOfAccount.find(_.endsWith(groups._1)), new NoSuchElementException("Unable to find a card ending with %s.".format(groups._1)))
      operationDate <- EitherT.fromEither[IO](computeOperationDate(groups._2, statementRow.date))
    } yield {
      Operation.ByCard(operationIdGenerator(), card, None, statementRow.libelle, statementRow.credit - statementRow.debit, operationDate, statementRow.dateValeur, statementRow.date)
    }
  }

  private def computeOperationDate(operationDateAsString: String, accountDate: LocalDate): Either[Throwable, LocalDate] = {
    (-1 to 1)
      .map(accountDate.getYear + _)
      .map { year =>
        Try {
          LocalDate.from(DateTimeFormatter.ofPattern("dd/MM/uuuu").parse("%s/%d".format(operationDateAsString, year)))
        }
          .toEither
      }
      .foldLeft(Either.right[Throwable](Seq.empty[LocalDate])) { (cumulDates, currentDate) =>
        cumulDates.flatMap(dates => currentDate.map(dates :+ _))
      }
      .map(_.sortBy(date => Math.abs(date.until(accountDate, DAYS))))
      .map(_.head)
  }

  implicit class ExtendedListOfEitherT[F[_] : Monad, A, B](listOfEitherT: List[EitherT[F, A, B]]) {
    def traverse: EitherT[F, A, List[B]] =
      listOfEitherT.foldLeft(EitherT.pure[F, A](List.empty[B])) { (eitherTOfList, eitherTOfElement) =>
        eitherTOfList.flatMap(list => eitherTOfElement.map(element => list :+ element))
      }
  }
}
