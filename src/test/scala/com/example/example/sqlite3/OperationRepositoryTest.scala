package com.example.example.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.NonEmptySeq
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers._
import com.example.example.business._

import java.sql.Connection

class OperationRepositoryTest
  extends AnyFreeSpecLike
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
      "should retrieve a card operation" in withDatabase { implicit connection: Connection =>
        withBanks(
          Bic("BANK", "FR", "01").map(Bank(_, "Demo bank")).get
        ) { implicit connection: Connection =>
          val select = SQL("SELECT COUNT(*) AS value FROM bank")
          val result = select.executeQuery().as[Int](intParser.single)
          result shouldEqual 1
        }
      }
    }
  }
}
