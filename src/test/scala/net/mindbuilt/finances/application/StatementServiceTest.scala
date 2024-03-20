package net.mindbuilt.finances.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import net.mindbuilt.finances.application.StatementServiceTest.cardRules
import net.mindbuilt.finances.business.{Operation, Statement}
import net.mindbuilt.finances.{IntToCents, TestHelpers}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.util.UUID
import scala.util.matching.Regex

class StatementServiceTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with TestHelpers
{
  "A statement row can be converted to an operation" - {
    
    "given a card payment" in {
      val statementRow = Statement.Row(2024\3\14, 2024\3\14, "PAIEMENT PAR CARTE X7279 ASF CONDRIEU VEDENE 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "5137707809237279"
      val expected = Operation.ByCard(
        operationId,
        cardNumber,
        None,
        "PAIEMENT PAR CARTE X7279 ASF CONDRIEU VEDENE 13/03",
        (-550).cents,
        2024\3\13,
        2024\3\14,
        2024\3\14
      )
      val actual = StatementService.statementRowToOperation(statementRow, Set(cardNumber), cardRules)
      actual.value.asserting(_.value shouldEqual expected)
    }

    "given a card payment at the end of the year" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "PAIEMENT PAR CARTE X7279 ASF CONDRIEU VEDENE 31/12", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "5137707809237279"
      val expected = Operation.ByCard(
        operationId,
        cardNumber,
        None,
        "PAIEMENT PAR CARTE X7279 ASF CONDRIEU VEDENE 31/12",
        (-550).cents,
        2023 \ 12 \ 31,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1
      )
      val actual = StatementService.statementRowToOperation(statementRow, Set(cardNumber), cardRules)
      actual.value.asserting(_.value shouldEqual expected)
    }

  }
  
  "A statement row cannot be converted to an operation" - {
    
    "when no card matches the captured suffix" in {
      val statementRow = Statement.Row(2024 \ 3 \ 14, 2024 \ 3 \ 14, "PAIEMENT PAR CARTE X0000 ASF CONDRIEU VEDENE 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "5137707809237279"
      val actual = StatementService.statementRowToOperation(statementRow, Set(cardNumber), cardRules)
      actual.value.asserting(_.left.value shouldBe a [NoSuchElementException])
    }

    "when no rules matches the row" in {
      val statementRow = Statement.Row(2024 \ 3 \ 14, 2024 \ 3 \ 14, "CAAAARTE X7279 ASF CONDRIEU VEDENE 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "5137707809237279"
      val actual = StatementService.statementRowToOperation(statementRow, Set(cardNumber), cardRules)
      actual.value.asserting(_.left.value shouldBe a [MatchError])
    }

  }
}

object StatementServiceTest {
  val cardRules: fs2.Stream[IO, Regex] = fs2.Stream.emits(Seq(
    raw"PAIEMENT PAR CARTE X(?<cardSuffix>\p{Digit}{4}) .* (?<incompleteDate>\p{Digit}{2}/\p{Digit}{2})\p{Space}*",
    raw"RETRAIT AU DISTRIBUTEUR X(?<cardSuffix>\p{Digit}{4}) .* (?<incompleteDate>\p{Digit}{2}/\p{Digit}{2}) \p{Digit}{2}H\p{Digit}{2}\p{Space}*"
  )
    .map(_.r))
}