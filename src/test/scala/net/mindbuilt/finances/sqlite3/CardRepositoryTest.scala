package net.mindbuilt.finances.sqlite3

import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.business.{Bank, Bic, Holder, Account, Iban, Card}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.time.Month._
import java.time.YearMonth
import java.util.UUID

class CardRepositoryTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with InMemoryDatabase
    with BankFixture
    with AccountFixture
{
  "CardRepository" - {
    "should save and retrieve all cards" in {
      val bank1 =  Bank(Bic("AGRI", "FR", "PP", "878").get, "CONDRIEU")
      val holder1 = Holder.Single(UUID.randomUUID(), "JOHN SMITH")
      val account1 = Account(bank1.bic, Iban("FR", "12", "123456789012345678901234567890").get, "CONDRIEU", holder1)
      val card1 = Card("1234", account1.iban, holder1.id, YearMonth.of(2024, JANUARY), "VISA")
      val card2 = Card("5678", account1.iban, holder1.id, YearMonth.of(2024, JANUARY), "Mastercard")
      val actual = withDatabase { implicit database: Database =>
        withBanks(bank1) { implicit database: Database =>
          withAccounts(account1) { implicit database: Database =>
            val sut = new CardRepository()
            for {
              _ <- sut.save(card1)
              _ <- sut.save(card2)
              retrievedCards <- sut.getAll
            } yield {
              retrievedCards
            }
          }
        }
      }
      actual.value.asserting(_.value should contain theSameElementsAs Seq(
        card1,
        card2
      ))
    }
    
    "should save and retrieve one card" in {
      val bank1 = Bank(Bic("AGRI", "FR", "PP", "878").get, "CONDRIEU")
      val holder1 = Holder.Single(UUID.randomUUID(), "JOHN SMITH")
      val account1 = Account(bank1.bic, Iban("FR", "12", "123456789012345678901234567890").get, "CONDRIEU", holder1)
      val card1 = Card("1234", account1.iban, holder1.id, YearMonth.of(2024, JANUARY), "VISA")
      val card2 = Card("5678", account1.iban, holder1.id, YearMonth.of(2024, JANUARY), "Mastercard")
      val actual = withDatabase { implicit database: Database =>
        withBanks(bank1) { implicit database: Database =>
          withAccounts(account1) { implicit database: Database =>
            val sut = new CardRepository()
            for {
              _ <- sut.save(card1)
              _ <- sut.save(card2)
              retrievedCard <- sut.getByNumber(card1.number)
            } yield {
              retrievedCard
            }
          }
        }
      }
      actual.value.asserting(_.value should contain(card1))
    }
  }

}
