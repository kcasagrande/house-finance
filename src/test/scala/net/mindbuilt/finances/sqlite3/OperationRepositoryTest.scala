package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.business._
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.sql.Connection

class OperationRepositoryTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with InMemoryDatabase
    with BankFixture
{
  implicit val intParser: RowParser[Int] = for {
    value <- int("value")
  } yield {
    value
  }
  
  "OperationRepository" - {
    "getById" - {
      "should save many operations of different types and retrieve them by interval" in {
        val actual = withDatabase { implicit database: Database =>
          withBanks(
            Bank(Bic("BANK", "FR", "01").get, "Demo bank"),
            Bank(Bic("DEMO", "FR", "02").get, "Demo bank 2")
          ) { implicit database: Database =>
            withConnection { implicit connection: Connection =>
              executeQueryWithEffect("SELECT COUNT(*) AS value FROM bank")
                .map(_.as[Int](intParser.single))
            }
          }
        }
        actual.value.asserting(_.value shouldBe 2)
      }
    }
  }
}
