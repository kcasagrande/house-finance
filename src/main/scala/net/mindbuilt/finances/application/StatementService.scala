package net.mindbuilt.finances.application

import cats.data.EitherT
import cats.effect.IO
import fs2.Stream
import fs2.data.csv._
import fs2.data.csv.lenient.attemptDecodeUsingHeaders
import fs2.io.file.{Files, Path}
import fs2.text.lines
import net.mindbuilt.finances.Helpers._
import net.mindbuilt.finances.application.StatementService._
import net.mindbuilt.finances.business.{AccountRepository, Card, CardRepository, Iban, Operation, OperationRepository, Statement}
import net.mindbuilt.finances.{Cents, IntToCents}

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.{Locale, UUID}
import scala.language.{implicitConversions, postfixOps}
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class StatementService(
  accountRepository: AccountRepository,
  operationRepository: OperationRepository,
  cardRepository: CardRepository
)(implicit
  operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
) {
  def `import`(account: Iban, content: Stream[IO, String]): EitherT[IO, Throwable, Seq[Operation]] = {
    val rulesDirectory = "src/main/resources/rules.d/"
    for {
      cardRules <- EitherT.liftF(Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get(rulesDirectory + "card.txt"))).through(lines).map(_.r).compile.toList)
      checkRules <- EitherT.liftF(Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get(rulesDirectory + "check.txt"))).through(lines).map(_.r).compile.toList)
      debitRules <- EitherT.liftF(Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get(rulesDirectory + "debit.txt"))).through(lines).map(_.r).compile.toList)
      transferRules <- EitherT.liftF(Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get(rulesDirectory + "transfer.txt"))).through(lines).map(_.r).compile.toList)
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
        .flatMap(statementRows => EitherT.fromEither[IO](statementRows.map { statementRow => statementRowToOperation(statementRow, account, cards.map(_.number), cardRules, checkRules, debitRules, transferRules) }.traverse))
    } yield {
      operations
    }
  }
}

object StatementService {

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

  private def buildCardOperation(
    cards: Set[Card.Number]
  )(implicit
    operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
  ): (Regex.Match, Statement.Row) => Either[Throwable, Operation.ByCard] = { (regexMatch, statementRow) =>
    for {
      cardSuffix <- regexMatch.groupEither("cardSuffix")
      incompleteDate <- regexMatch.groupEither("incompleteDate")
      card <- cards.find(_.endsWith(cardSuffix)).toRight(new NoSuchElementException("Unable to find a card ending with %s.".format(cardSuffix)))
      operationDate <- computeOperationDate(incompleteDate, statementRow.date)
    } yield {
      Operation.ByCard(
        operationIdGenerator(),
        card,
        None,
        statementRow.libelle,
        statementRow.credit - statementRow.debit,
        operationDate,
        statementRow.dateValeur,
        statementRow.date
      )
    }
  }

  private def buildCheckOperation(
    account: Iban
  )(
    implicit
    operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
  ): (Regex.Match, Statement.Row) => Either[Throwable, Operation.ByCheck] = { (regexMatch, statementRow) =>
    for {
      checkNumber <- regexMatch.groupEither("checkNumber")
    } yield {
      Operation.ByCheck(
        operationIdGenerator(),
        account,
        checkNumber,
        statementRow.libelle,
        statementRow.credit - statementRow.debit,
        statementRow.dateValeur,
        statementRow.dateValeur,
        statementRow.date
      )
    }
  }

  private def buildDebitOperation(
    account: Iban
  )(implicit
    operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
  ): (Regex.Match, Statement.Row) => Either[Throwable, Operation.ByDebit] = { (_, statementRow) =>
    Either.right[Throwable](
      Operation.ByDebit(
        operationIdGenerator(),
        account,
        None,
        statementRow.libelle,
        statementRow.credit - statementRow.debit,
        statementRow.dateValeur,
        statementRow.dateValeur,
        statementRow.date
      )
    )
  }
  
  private def buildTransferOperation(
    account: Iban
  )(implicit
    operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
  ): (Regex.Match, Statement.Row) => Either[Throwable, Operation.ByTransfer] = { (_, statementRow) =>
    Either.right[Throwable](
      Operation.ByTransfer(
        operationIdGenerator(),
        account,
        None,
        statementRow.libelle,
        statementRow.credit - statementRow.debit,
        statementRow.dateValeur,
        statementRow.dateValeur,
        statementRow.date,
        None
      )
    )
  }
  
  def statementRowToOperation(
    statementRow: Statement.Row,
    account: Iban,
    cardsOfAccount: Set[Card.Number] = Set.empty[Card.Number],
    cardRules: Seq[Regex],
    checkRules: Seq[Regex],
    debitRules: Seq[Regex],
    transferRules: Seq[Regex]
  )(implicit
    operationIdGenerator: () => Operation.Id
  ): Either[Throwable, Operation] = {
    val matchError: Throwable = new MatchError("No rule matching row label \"%s\".".format(statementRow.libelle))
    val allRules = (
      cardRules.map(_ -> buildCardOperation(cardsOfAccount)) ++
      checkRules.map(_ -> buildCheckOperation(account)) ++
      debitRules.map(_ -> buildDebitOperation(account)) ++
      transferRules.map(_ -> buildTransferOperation(account))
    ).toMap
    allRules.find(_._1.matches(statementRow.libelle))
      .flatMap {
        case (regex, buildOperation) => regex.findFirstMatchIn(statementRow.libelle)
          .map(buildOperation(_, statementRow))
      }
      .getOrElse(Either.left[Operation](matchError))
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

}
