package net.mindbuilt.finances.sqlite3

import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.business.{Account, Bank, Bic, Holder, Iban}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.util.UUID

class AccountRepositoryTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with InMemoryDatabase
    with BankFixture
{
  val holderId1: Holder.Id = UUID.randomUUID()
  val holderId2: Holder.Id = UUID.randomUUID()
  val banks: Seq[Bank] = Seq(
    Bank(Bic("AGRI", "FR", "PP", "878").get, "CONDRIEU"),
    Bank(Bic("CCBP", "FR", "PP", "GRE").get, "BPAURA CONDRIEU")
  )
  "AccountRepository" - {
    "should save and retrieve an account with a single holder" in {
      val iban1 = Iban("FR", "12", "123456789012345678901234567890").get
      val iban2 = Iban("FR", "21", "098765432109876543210987654321").get
      val account1 = Account(banks.head.bic, iban1, "CONDRIEU", Holder.Single(holderId1, "JOHN SMITH"))
      val account2 = Account(banks.last.bic, iban2, "BPAURA CONDRIEU", Holder.Single(holderId2, "SOMEONE ELSE"))
      val actual = withDatabase { implicit database: Database =>
        withBanks(banks.head, banks.tail: _*) { implicit database: Database =>
          val sut = new AccountRepository()
          for {
            _ <- sut.save(account1)
            _ <- sut.save(account2)
            retrievedAccount <- sut.getByIban(iban1)
          } yield {
            retrievedAccount
          }
        }
      }
      actual.value.asserting(_.value should contain(account1))
    }
  }
}