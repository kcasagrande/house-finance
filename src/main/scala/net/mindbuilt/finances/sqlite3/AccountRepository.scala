package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Holder.Multiple.Combination
import net.mindbuilt.finances.business.Holder.Single
import net.mindbuilt.finances.business.{Account, Holder, Iban, AccountRepository => Port}
import net.mindbuilt.finances.sqlite3.AccountRepository.{account, ibanToNamedParameters, singleHolder}

import java.sql.Connection
import scala.language.implicitConversions

class AccountRepository(implicit val database: Database)
  extends Port
{
  override def getAll: EitherT[IO, Throwable, Set[Account]] =
    withConnection { implicit connection: Connection =>
      for {
        holders <- getAllHolders
        accounts <- getAllAccounts(holders)
      } yield {
        accounts
      }
    }
  
  override def getByIban(iban: Iban): EitherT[IO, Throwable, Option[Account]] =
    withConnection { implicit connection: Connection =>
      for {
        holderOption <- getHolder(iban)
        account <- holderOption match {
          case None => EitherT.pure[IO, Throwable][Option[Account]](None)
          case Some(holder) => getAccount(iban, holder)
        }
      } yield {
        account
      }
    }

  private[this] def getAllAccounts(holders: Map[Iban, Holder])(implicit connection: Connection): EitherT[IO, Throwable, Set[Account]] = {
    executeQueryWithEffect(
      """SELECT
        |  "bank",
        |  "country_code",
        |  "check_digits",
        |  "bban",
        |  "domiciliation"
        |FROM "account"
        |""".stripMargin,
    )(account(holders).set)
  }
  
  private[this] def getAccount(iban: Iban, holder: Holder)(implicit connection: Connection): EitherT[IO, Throwable, Option[Account]] = {
    executeQueryWithEffect(
      """SELECT
        |  "bank",
        |  "country_code",
        |  "check_digits",
        |  "bban",
        |  "domiciliation"
        |FROM "account"
        |WHERE "country_code"={country_code}
        |  AND "check_digits"={check_digits}
        |  AND "bban"={bban}""".stripMargin,
      iban:_*
    )(account(holder).singleOpt)
  }

  private[this] def getAllHolders(implicit connection: Connection): EitherT[IO, Throwable, Map[Iban, Holder]] = {
    for {
      combinations <- executeQueryWithEffect(
        """SELECT
          |  "account_country_code",
          |  "account_check_digits",
          |  "account_bban",
          |  "combination"
          |FROM "multiple_account_holder"
          |""".stripMargin
      )(
        (iban("account_country_code", "account_check_digits", "account_bban")
          ~ AccountRepository.combination("combination")
          map { case iban ~ combination => (iban, combination) })
          .*.map(_.toMap)
      )
      multipleHolders <- executeQueryWithEffect(
        """SELECT
          |  "i"."account_country_code",
          |  "i"."account_check_digits",
          |  "i"."account_bban",
          |  "h"."id",
          |  "h"."name"
          |FROM "individual_holder_for_multiple_account_holder" "i"
          |INNER JOIN "holder" "h"
          |ON "i"."holder"="h"."id"
          |""".stripMargin
      )(
        (iban("account_country_code", "account_check_digits", "account_bban")
          ~ singleHolder
          map { case iban ~ holder => (iban, holder) })
          .set
      )
      singleHolders <- executeQueryWithEffect(
        """SELECT
          |  "s"."account_country_code",
          |  "s"."account_check_digits",
          |  "s"."account_bban",
          |  "h"."id",
          |  "h"."name"
          |FROM "single_account_holder" "s"
          |INNER JOIN "holder" "h"
          |ON "s"."holder"="h"."id"
          |""".stripMargin
      )(
        (iban("account_country_code", "account_check_digits", "account_bban")
          ~ singleHolder
          map { case iban ~ holder => (iban, holder) })
          .set
      )
    } yield {
      combinations.map {
        case (iban, combination) => iban -> combination.of(multipleHolders.filter(_._1 == iban).map(_._2))
      } ++ singleHolders
    }
  }

  private[this] def getHolder(iban: Iban)(implicit connection: Connection): EitherT[IO, Throwable, Option[Holder]] = {
    executeQueryWithEffect(
      """SELECT
        |  "m"."combination"
        |FROM "multiple_account_holder" "m"
        |WHERE "m"."account_country_code"={country_code}
        |AND "m"."account_check_digits"={check_digits}
        |AND "m"."account_bban"={bban}"""
        .stripMargin,
      iban: _*
    )(AccountRepository.combination("combination").singleOpt)
      .flatMap {
        case None => executeQueryWithEffect(
          """SELECT
            |  "h"."id" AS "id",
            |  "h"."name" AS "name"
            |FROM "single_account_holder" "s"
            |INNER JOIN "holder" "h"
            |ON "s"."holder"="h"."id"
            |WHERE "s"."account_country_code"={country_code}
            |AND "s"."account_check_digits"={check_digits}
            |AND "s"."account_bban"={bban}""".stripMargin,
          iban: _*
        )(singleHolder.singleOpt)
        case Some(combination) => executeQueryWithEffect(
          """SELECT
            |  "h"."id",
            |  "h"."name"
            |FROM "individual_holder_for_multiple_account_holder" "i"
            |INNER JOIN "holder" "h"
            |ON "i"."holder"="h"."id"
            |WHERE "i"."account_country_code"={country_code}
            |AND "i"."account_check_digits"={check_digits}
            |AND "i"."account_bban"={bban}"""
            .stripMargin,
          iban: _*
        )(singleHolder.*)
          .map {
            case Nil => None
            case holders => Some(combination.of(holders.toSet))
          }
      }
  }

  override def save(account: Account): EitherT[IO, Throwable, Unit] = {
    withConnection { implicit connection: Connection =>
      for {
        _ <- saveAccount(account)
        _ <- account.holder match {
          case singleHolder: Holder.Single => saveSingleAccountHolder(account.iban, singleHolder)
          case multipleHolder: Holder.Multiple => saveMultipleAccountHolder(account.iban, multipleHolder)
        }
      } yield {
        ()
      }
    }
  }

  private[this] def saveAccount(account: Account)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    executeWithEffect(
      """INSERT INTO "account"("bank", "country_code", "check_digits", "bban", "domiciliation")
        |VALUES({bank}, {country_code}, {check_digits}, {bban}, {domiciliation})""".stripMargin,
      namedParameters(
        "bank" -> account.bank.toString,
        "country_code" -> account.iban.countryCode,
        "check_digits" -> account.iban.checkDigits,
        "bban" -> account.iban.bban,
        "domiciliation" -> account.domiciliation
      ):_*
    )

  private[this] def saveSingleAccountHolder(
    account: Iban,
    holder: Holder.Single
  )(implicit
    connection: Connection
  ): EitherT[IO, Throwable, Unit] =
    for {
      _ <- saveHolder(holder)
      _ <- executeWithEffect(
             """INSERT INTO "single_account_holder"("account_country_code", "account_check_digits", "account_bban", "holder")
                |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {holder})""".stripMargin,
             namedParameters(
               "account_country_code" -> account.countryCode,
               "account_check_digits" -> account.checkDigits,
               "account_bban" -> account.bban,
               "holder" -> holder.id.toString
             ):_*
           )
    } yield {
      ()
    }

  private[this] def saveMultipleAccountHolder(
    account: Iban,
    holder: Holder.Multiple
  )(implicit
    connection: Connection
  ): EitherT[IO, Throwable, Unit] =
    for {
      _ <- holder.individuals.map(saveHolder)
        .foldLeft(EitherT.pure[IO, Throwable](())) { (accumulator, item) =>
          accumulator.flatMap(_ => item)
        }
      _ <- saveMultipleHolderCombination(account, holder)
      _ <- holder.individuals.map(saveIndividualAccountHolder(account, _))
        .foldLeft(EitherT.pure[IO, Throwable](())){ (accumulator, item) =>
          accumulator.flatMap(_ => item)
        }
    } yield {
      ()
    }

  private[this] def saveHolder(holder: Holder.Single)(implicit connection: Connection): EitherT[IO, Throwable, Unit] =
    executeWithEffect(
      """INSERT OR IGNORE INTO "holder"("id", "name")
        |VALUES ({id}, {name})""".stripMargin,
      holder: _ *
    )

  private[this] def saveMultipleHolderCombination(
    account: Iban,
    holder: Holder.Multiple
  )(
    implicit connection: Connection
  ): EitherT[IO, Throwable, Unit] =
    executeWithEffect(
      """INSERT INTO "multiple_account_holder"("account_country_code", "account_check_digits", "account_bban", "combination")
        |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {combination})""".stripMargin,
      namedParameters(
        "account_country_code" -> account.countryCode,
        "account_check_digits" -> account.checkDigits,
        "account_bban" -> account.bban,
        "combination" -> holder.combination.separator
      ): _*
    )

  private[this] def saveIndividualAccountHolder(
    account: Iban,
    holder: Holder.Single
  )(implicit
    connection: Connection
  ): EitherT[IO, Throwable, Unit] =
    executeWithEffect(
      """INSERT INTO "individual_holder_for_multiple_account_holder"("account_country_code", "account_check_digits", "account_bban", "holder")
        |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {holder})""".stripMargin,
      namedParameters(
        "account_country_code" -> account.countryCode,
        "account_check_digits" -> account.checkDigits,
        "account_bban" -> account.bban,
        "holder" -> holder.id.toString
      ): _*
    )
}

object AccountRepository {
  implicit def ibanToNamedParameters(iban: Iban): Seq[NamedParameter] =
    namedParameters(
      "country_code" -> iban.countryCode,
      "check_digits" -> iban.checkDigits,
      "bban" -> iban.bban
    )
    
  val singleHolder: RowParser[Single] = for {
    id <- uuid("id")
    name <- str("name")
  } yield {
    Holder.Single(id, name)
  }
  
  def combination(columnName: String): RowParser[Combination] = (row: Row) => str(columnName).apply(row)
    .flatMap {
      case Combination.And.separator => anorm.Success(Combination.And)
      case Combination.Or.separator => anorm.Success(Combination.Or)
      case anythingElse => Error(SqlRequestError(new IllegalArgumentException(anythingElse + " is not a combination word")))
    }
  
  def account(holder: Holder): RowParser[Account] = for {
    bank <- bic("bank")
    iban <- iban("country_code", "check_digits", "bban")
    domiciliation <- str("domiciliation")
  } yield {
    Account(bank, iban, domiciliation, holder)
  }

  def account(holders: Map[Iban, Holder]): RowParser[Account] = for {
    bank <- bic("bank")
    iban <- iban("country_code", "check_digits", "bban")
    domiciliation <- str("domiciliation")
  } yield {
    Account(bank, iban, domiciliation, holders(iban))
  }

}
