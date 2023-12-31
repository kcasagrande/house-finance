package net.mindbuilt.finances.sqlite3

import cats.data.EitherT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.business.LocalInterval.LocalIntervalBoundary
import net.mindbuilt.finances.business.Operation
import net.mindbuilt.finances.sqlite3.StandardFixture.{accounts, cards, individualHolders}
import net.mindbuilt.finances.{IntToCents, TestHelpers}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.time.Month.AUGUST
import java.util.UUID

class OperationRepositoryTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with InMemoryDatabase
    with StandardFixture
    with TestHelpers
{
  val operations: Seq[Operation] = Seq(
    Operation.ByCard(
      UUID.randomUUID(),
      cards.head.number,
      Some("12345670"),
      "Paiement 0 par carte",
      2023 - AUGUST - 1,
      2023 - AUGUST - 2,
      2023 - AUGUST - 3,
      Seq(
        Operation.Breakdown(300.cents, Some("Maison"), Some("30%"), Some(individualHolders(0).id)),
        Operation.Breakdown(700.cents, Some("Maison"), Some("70%"), Some(individualHolders(1).id))
      )
    ),
    Operation.ByCheck(
      UUID.randomUUID(),
      accounts.head.iban,
      "12345671",
      "Paiement 1 par chèque",
      2023 - AUGUST - 4,
      2023 - AUGUST - 5,
      2023 - AUGUST - 6,
      Seq(
        Operation.Breakdown(500.cents, Some("Restaurant"), None, Some(individualHolders(0).id)),
        Operation.Breakdown(500.cents, Some("Restaurant"), None, Some(individualHolders(1).id))
      )
    ),
    Operation.ByDebit(
      UUID.randomUUID(),
      accounts.head.iban,
      Some("12345672"),
      "Paiement 3 par prélèvement",
      2023 - AUGUST - 7,
      2023 - AUGUST - 8,
      2023 - AUGUST - 9,
      Seq(
        Operation.Breakdown(1000.cents, Some("Électricité"), None, None)
      )
    ),
    Operation.ByTransfer(
      UUID.randomUUID(),
      accounts.head.iban,
      Some("12345672"),
      "Paiement 3 par prélèvement",
      1000.cents,
      2023 - AUGUST - 10,
      2023 - AUGUST - 11,
      2023 - AUGUST - 12,
      Some(accounts(1).iban)
    )
  )
  "OperationRepository" - {
    "should save many operations of different types and retrieve them by interval" in {
      val actual = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withStandardFixture { implicit database: EitherT[IO, Throwable, Database] =>
          val sut = new OperationRepository
          for {
            _ <- operations.map(sut.save).foldLeft(IO.pure(())) { (io, result) => io <& result }
            retrievedOperations <- sut.getByInterval(2023 - AUGUST - 1 to 2023 - AUGUST - 9)
          } yield {
            retrievedOperations
          }
        }
      }
      actual.value.asserting(_.value should contain theSameElementsAs operations.take(3))
    }

    "should save many operations of different types and retrieve one of them by ID" in {
      val actual = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withStandardFixture { implicit database: EitherT[IO, Throwable, Database] =>
          val sut = new OperationRepository
          for {
            _ <- operations.map(sut.save).foldLeft(IO.pure(())) { (io, result) => io <& result }
            retrievedOperation <- sut.getById(operations.head.id)
          } yield {
            retrievedOperation
          }
        }
      }
      actual.value.asserting(_.value should contain(operations.head))
    }

    "should get all categories of existing operations" in {
      val actual = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withStandardFixture { implicit database: EitherT[IO, Throwable, Database] =>
          val sut = new OperationRepository
          for {
            _ <- operations.map(sut.save).foldLeft(IO.pure(())) { (io, result) => io <& result }
            retrievedCategories <- sut.getAllCategories
          } yield {
            retrievedCategories
          }
        }
      }
      actual.value.asserting(_.value should contain theSameElementsAs Set(
        "Maison",
        "Restaurant",
        "Électricité"
      ))
    }
  }
}
