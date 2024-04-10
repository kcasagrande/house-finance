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
import net.mindbuilt.finances.business.{Card, CardRepository, Iban, Operation, Statement}
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
  cardRepository: CardRepository
)(implicit
  operationIdGenerator: () => Operation.Id = () => UUID.randomUUID()
) {
  private[this] val rulesDirectory = "src/main/resources/rules.d/"
  private[this] def loadRulesFromFile(fileName: String): EitherT[IO, Throwable, List[Regex]] =
    EitherT.liftF(Files[IO].readUtf8(Path.fromNioPath(java.nio.file.Paths.get(rulesDirectory + fileName))).through(lines).map(_.r).compile.toList)
  
  def `import`(account: Iban, content: Stream[IO, String]): EitherT[IO, Throwable, Seq[Operation]] = {
    for {
      cardRules <- loadRulesFromFile("card.txt")
      checkRules <- loadRulesFromFile("check.txt")
      debitRules <- loadRulesFromFile("debit.txt")
      transferRules <- loadRulesFromFile("transfer.txt")
      cards <- cardRepository.getByAccount(account)
      operations <- EitherT(content
        .through(attemptDecodeUsingHeaders[Statement.Row](';'))
        .compile.toList
        .map(_.toSeq)
        .map(eitherThrowablesToEitherCompositeThrowable)
      )
        .flatMap(statementRows => EitherT.fromEither[IO](statementRows.map { statementRow => statementRowToOperation(statementRow, account, cards.map(_.number), cardRules, checkRules, debitRules, transferRules) }.traverse))
    } yield {
      operations
    }
  }
  
  private[this] def parseRow(
    row: Statement.Row,
    rules: Seq[Regex]
  ): Statement.ParsedRow = {
    val matches = rules.find(_.matches(row.libelle))
      .flatMap(_.findFirstMatchIn(row.libelle))
      .getOrElse("".r.findFirstMatchIn("").get)
    Statement.ParsedRow(
      label = row.libelle,
      credit = row.credit - row.debit,
      accountDate = row.date,
      valueDate = row.dateValeur,
      `type` = matches.groupOption("card").map(_ => classOf[Operation.ByCard])
        .orElse(matches.groupOption("check").map(_ => classOf[Operation.ByCheck]))
        .orElse(matches.groupOption("debit").map(_ => classOf[Operation.ByDebit]))
        .orElse(matches.groupOption("transfer").map(_ => classOf[Operation.ByTransfer])),
      reference = matches.groupOption("reference"),
      operationDate = matches.groupOption("incompleteDate").flatMap(computeOperationDate(_, row.date).toOption),
      cardSuffix = matches.groupOption("cardSuffix"),
      checkNumber = matches.groupOption("checkNumber")
    )
  }

  def parse(content: Stream[IO, String]): EitherT[IO, Throwable, Seq[Statement.ParsedRow]] =
    for {
      rules <- loadRulesFromFile("rules.txt")
      rows <- EitherT[IO, Throwable, Seq[Statement.Row]](
        content.through(attemptDecodeUsingHeaders[Statement.Row](';')).compile.toList
          .map(_.toSeq)
          .map(eitherThrowablesToEitherCompositeThrowable)
      )
      parsedRows = rows.map(parseRow(_, rules))
    } yield {
      parsedRows
    }
}

object StatementService {

  case class CompositeThrowable(underlyingThrowables: Seq[Throwable])
    extends Throwable {
    override def getMessage: String = underlyingThrowables.map(_.getMessage).mkString("\n")
    def :+(throwable: Throwable): CompositeThrowable = this.copy(underlyingThrowables = this.underlyingThrowables :+ throwable)
  }
  implicit def throwableToCompositeThrowable(throwable: Throwable): CompositeThrowable = CompositeThrowable(Seq(throwable))
  implicit def eitherThrowablesToEitherCompositeThrowable[T](eitherThrowables: Seq[Either[Throwable, T]]): Either[CompositeThrowable, Seq[T]] =
    eitherThrowables.foldLeft(Either.right[CompositeThrowable](Seq.empty[T])) { (result, element) =>
      result match {
        case left@Left(throwables) =>
          element match {
            case Left(throwable) => Left(throwables :+ throwable)
            case Right(_) => left
          }
        case Right(values) =>
          element match {
            case Left(throwable) => Left(throwable)
            case Right(value) => Either.right[CompositeThrowable](values :+ value)
          }
      }
    }

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
