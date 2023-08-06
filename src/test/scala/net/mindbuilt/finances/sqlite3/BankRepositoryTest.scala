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
    "should save and then retrieve a bank" in {
      val actual = withDatabase { implicit database: Database =>
        val sut = new BankRepository()
        for {
          bic1 <- EitherT.fromEither[IO](Bic("AGRI", "FR", "PP", "878").toEither)
          bank1 = Bank(bic1, "CONDRIEU")
          _ <- sut.save(bank1)
          bic2 <- EitherT.fromEither[IO](Bic("CCBP", "FR", "PP", "GRE").toEither)
          bank2 = Bank(bic2, "BPAURA CONDRIEU")
          _ <- sut.save(bank2)
          retrievedBank <- sut.getByBic(bic1)
        } yield {
          retrievedBank
        }
      }
      actual.value.asserting(_.value should contain(Bank(Bic("AGRI", "FR", "PP", "878").get, "CONDRIEU")))
    }
  }
}
