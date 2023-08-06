package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.business.{Bank, Bic}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

class BankRepositoryTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with InMemoryDatabase
    with EitherValues
{
  "BankRepository" - {
    "should save then retrieve a bank" in {
      val bic1 = Bic("AGRI", "FR", "PP", "878").get
      val bank1 = Bank(bic1, "CONDRIEU")
      val bic2 = Bic("CCBP", "FR", "PP", "GRE").get
      val bank2 = Bank(bic2, "BPAURA CONDRIEU")
      val actual = withDatabase { implicit database: Database =>
        val sut = new BankRepository()
        for {
          _ <- sut.save(bank1)
          _ <- sut.save(bank2)
          retrievedBank <- sut.getByBic(bic1)
        } yield {
          retrievedBank
        }
      }
      actual.value.asserting(_.value should contain(bank1))
    }
  }

  "should save then retrieve all banks" in {
    val bic1 = Bic("AGRI", "FR", "PP", "878").get
    val bank1 = Bank(bic1, "CONDRIEU")
    val bic2 = Bic("CCBP", "FR", "PP", "GRE").get
    val bank2 = Bank(bic2, "BPAURA CONDRIEU")
    val actual = withDatabase { implicit database: Database =>
      val sut = new BankRepository()
      for {
        _ <- sut.save(bank1)
        _ <- sut.save(bank2)
        retrievedBanks <- sut.getAll
      } yield {
        retrievedBanks
      }
    }
    actual.value.asserting(_.value should contain theSameElementsAs Seq(
      bank1,
      bank2
    ))
  }
}
