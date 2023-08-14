package net.mindbuilt.finances.sqlite3

import anorm.NamedParameter
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Account, Bank, Bic, Card, Holder, Iban}
import net.mindbuilt.finances.sqlite3.StandardFixture._

import java.sql.Connection
import java.time.Month._
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

trait StandardFixture { self: InMemoryDatabase =>
  def withStandardFixture[T]
    (test: EitherT[IO, Throwable, Database] => IO[T])
    (implicit database: EitherT[IO, Throwable, Database]): IO[T] =
  {
    withConnection { implicit connection: Connection =>
      (
        for {
          _ <- executeBatchWithEffect(
            """INSERT INTO "bank"("bic", "designation") VALUES ({bic}, {designation})""",
            bankToNamedParameters(banks.head),
            banks.tail.map(bankToNamedParameters):_*
          )
          _ <- executeBatchWithEffect(
            """INSERT INTO "holder"("id", "name")
              |VALUES ({id}, {name})""".stripMargin,
            individualHolderToNamedParameters(individualHolders.head),
            individualHolders.tail.map(individualHolderToNamedParameters): _*
          )
          _ <- executeBatchWithEffect(
            """INSERT INTO "account"("bank", "country_code", "check_digits", "bban", "domiciliation")
              |VALUES ({bank}, {country_code}, {check_digits}, {bban}, {domiciliation})""".stripMargin,
            accountToNamedParameters(accounts.head),
            accounts.tail.map(accountToNamedParameters): _*
          )
          singleHolderAccounts = accounts.filter(_.holder.isInstanceOf[Holder.Single])
          _ <- executeBatchWithEffect(
            """INSERT INTO "single_account_holder"("account_country_code", "account_check_digits", "account_bban", "holder")
              |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {holder})""".stripMargin,
            singleAccountHolderToNamedParameters(singleHolderAccounts.head),
            singleHolderAccounts.map(singleAccountHolderToNamedParameters):_*
          )
          multipleHolderAccounts = accounts.filter(_.holder.isInstanceOf[Holder.Multiple])
          _ <- executeBatchWithEffect(
            """INSERT INTO "multiple_account_holder"("account_country_code", "account_check_digits", "account_bban", "combination")
              |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {combination})""".stripMargin,
            multipleAccountHolderToNamedParameters(multipleHolderAccounts.head),
            multipleHolderAccounts.map(multipleAccountHolderToNamedParameters): _*
          )
          individualHoldersForMultipleHolderAccounts = accounts.flatMap(account => account.asInstanceOf[Holder.Multiple].individuals.map(account.iban -> _))
          _ <- executeBatchWithEffect(
            """INSERT INTO "individual_holder_for_multiple_account_holder"("account_country_code", "account_check_digits", "account_bban", "holder")
              |VALUES ({account_country_code}, {account_check_digits}, {account_bban}, {holder})""".stripMargin,
            individualHolderForMultipleAccountHolderToNamedParameters.tupled(individualHoldersForMultipleHolderAccounts.head),
            individualHoldersForMultipleHolderAccounts.map(individualHolderForMultipleAccountHolderToNamedParameters.tupled): _*
          )
          _ <- executeBatchWithEffect(
            """INSERT INTO "card_type"("name")
              |VALUES({name})""".stripMargin,
            cardTypeToNamedParameters(cardTypes.head),
            cardTypes.tail.map(cardTypeToNamedParameters):_*
          )
          _ <- executeBatchWithEffect(
            """INSERT INTO "card"("number", "account_country_code", "account_check_digits", "account_bban", "holder", "expiration", "type")
              |VALUES({number}, {account_country_code}, {account_check_digits}, {account_bban}, {holder}, {expiration}, {type})""".stripMargin,
            cardToNamedParameters(cards.head),
            cards.tail.map(cardToNamedParameters):_*
          )
        } yield ()
      ).value
    }
    test(database)
  }
}

object StandardFixture {
  val banks: Seq[Bank] = Seq(
    Bank(Bic("STDA", "FR", "00").get, "Standard fixture bank 0"),
    Bank(Bic("STDB", "FR", "01").get, "Standard fixture bank 1")
  )
  val holders: Seq[Holder] = Seq(
    Holder.Single(UUID.randomUUID(), "Standard fixture single holder 0"),
    Holder.Multiple.allOf(Set(
      Holder.Single(UUID.randomUUID(), "Standard fixture multiple AND holder 1.0"),
      Holder.Single(UUID.randomUUID(), "Standard fixture multiple AND holder 1.1")
    )),
    Holder.Multiple.oneOf(Set(
      Holder.Single(UUID.randomUUID(), "Standard fixture multiple OR holder 2.0"),
      Holder.Single(UUID.randomUUID(), "Standard fixture multiple OR holder 2.1")
    ))
  )
  val individualHolders: Seq[Holder.Single] = holders
    .collect {
      case single: Holder.Single => Seq(single)
      case multiple: Holder.Multiple => multiple.individuals.toSeq
    }
    .flatten
  val accounts: Seq[Account] = Seq(
    Account(banks(0).bic, Iban("FR", "01", "123456789012345678901234567890").get, "Standard fixture account 0", holders(0)),
    Account(banks(0).bic, Iban("FR", "01", "123456789012345678901234567891").get, "Standard fixture account 1", holders(1)),
    Account(banks(1).bic, Iban("FR", "01", "123456789012345678901234567892").get, "Standard fixture account 2", holders(2))
  )
  val cardTypes: Seq[Card.Type] = Seq(
    "VISA",
    "Eurocard"
  )
  val cards: Seq[Card] = Seq(
    Card("1234123412340000", accounts(0).iban, individualHolders(0).id, YearMonth.of(2025, MAY), "VISA"),
    Card("1234123412340001", accounts(1).iban, individualHolders(1).id, YearMonth.of(2025, MAY), "VISA"),
    Card("1234123412340002", accounts(2).iban, individualHolders(3).id, YearMonth.of(2025, MAY), "Eurocard")
  )
  
  private def accountToNamedParameters(account: Account): Seq[NamedParameter] =
    namedParameters(
      "bank" -> account.bank.toString,
      "country_code" -> account.iban.countryCode,
      "check_digits" -> account.iban.checkDigits,
      "bban" -> account.iban.bban,
      "domiciliation" -> account.domiciliation
    )
    
  private def individualHolderToNamedParameters(holder: Holder.Single): Seq[NamedParameter] =
    namedParameters(
      "id" -> holder.id.toString,
      "name" -> holder.name
    )
    
  private def singleAccountHolderToNamedParameters(account: Account): Seq[NamedParameter] =
    namedParameters(
      "account_country_code" -> account.iban.countryCode,
      "account_check_digits" -> account.iban.checkDigits,
      "account_bban" -> account.iban.bban,
      "holder" -> account.holder.asInstanceOf[Holder.Single].name
    )

  private def multipleAccountHolderToNamedParameters(account: Account): Seq[NamedParameter] =
    namedParameters(
      "account_country_code" -> account.iban.countryCode,
      "account_check_digits" -> account.iban.checkDigits,
      "account_bban" -> account.iban.bban,
      "combination" -> account.holder.asInstanceOf[Holder.Multiple].combination.separator
    )

  private val individualHolderForMultipleAccountHolderToNamedParameters: (Iban, Holder.Single) => Seq[NamedParameter] =
    (iban: Iban, holder: Holder.Single) =>
      namedParameters(
        "account_country_code" -> iban.countryCode,
        "account_check_digits" -> iban.checkDigits,
        "account_bban" -> iban.bban,
        "holder" -> holder.id.toString
      )

  private val cardTypeToNamedParameters: Card.Type => Seq[NamedParameter] =
    (cardType: Card.Type) =>
      namedParameters(
        "name" -> cardType
      )
    
  private val cardToNamedParameters: Card => Seq[NamedParameter] =
    (card: Card) =>
      namedParameters(
        "number" -> card.number,
        "account_country_code" -> card.account.countryCode,
        "account_check_digits" -> card.account.checkDigits,
        "account_bban" -> card.account.bban,
        "holder" -> card.holder,
        "expiration" -> DateTimeFormatter.ofPattern("uuuu-MM").format(card.expiration),
        "type" -> card.`type`
      )
}