package net.mindbuilt.finances.application

import net.mindbuilt.finances.application.StatementServiceTest._
import net.mindbuilt.finances.business.{Card, Iban, Operation, Statement}
import net.mindbuilt.finances.{IntToCents, TestHelpers}
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.util.UUID
import scala.util.matching.Regex

class StatementServiceTest
  extends AsyncFreeSpecLike
    with EitherValues
    with TestHelpers
{
  "A statement row can be converted to an operation" - {
    
    "given a card payment" in {
      val statementRow = Statement.Row(2024\3\14, 2024\3\14, "PAIEMENT PAR CARTE X1234 BLABLABLA BLABLA 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "1023871032941234"
      val expected = Operation.ByCard(
        operationId,
        cardNumber,
        None,
        "PAIEMENT PAR CARTE X1234 BLABLABLA BLABLA 13/03",
        (-550).cents,
        2024\3\13,
        2024\3\14,
        2024\3\14
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set(cardNumber), cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }

    "given a card payment at the end of the year" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "PAIEMENT PAR CARTE X1234 BLABLABLA BLABLA 31/12", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "1293793047131234"
      val expected = Operation.ByCard(
        operationId,
        cardNumber,
        None,
        "PAIEMENT PAR CARTE X1234 BLABLABLA BLABLA 31/12",
        (-550).cents,
        2023 \ 12 \ 31,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set(cardNumber), cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }
    
    "given a check" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "CHEQUE EMIS 1234567", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val expected = Operation.ByCheck(
        operationId,
        account,
        "1234567",
        "CHEQUE EMIS 1234567",
        (-550).cents,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set.empty[Card.Number], cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }

    "given a payment by debit" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "PRELEVEMENT ALEATOIRE", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val expected = Operation.ByDebit(
        operationId,
        account,
        None,
        "PRELEVEMENT ALEATOIRE",
        (-550).cents,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set.empty[Card.Number], cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }

    "given a payment by transfer" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "VIREMENT EMIS BLABLABLA BLABLA", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val expected = Operation.ByTransfer(
        operationId,
        account,
        None,
        "VIREMENT EMIS BLABLABLA BLABLA",
        (-550).cents,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        None
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set.empty[Card.Number], cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }

    "given a credit by transfer" in {
      val statementRow = Statement.Row(2024 \ 1 \ 1, 2024 \ 1 \ 1, "VIREMENT EN VOTRE FAVEUR BLABLABLA BLABLA", 0.cents, 550.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val expected = Operation.ByTransfer(
        operationId,
        account,
        None,
        "VIREMENT EN VOTRE FAVEUR BLABLABLA BLABLA",
        550.cents,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        2024 \ 1 \ 1,
        None
      )
      val actual = StatementService.statementRowToOperation(statementRow, account, Set.empty[Card.Number], cardRules, checkRules, debitRules, transferRules)
      actual.value shouldEqual expected
    }

  }
  
  "A statement row cannot be converted to an operation" - {
    
    "when no card matches the captured suffix" in {
      val statementRow = Statement.Row(2024 \ 3 \ 14, 2024 \ 3 \ 14, "PAIEMENT PAR CARTE X0000 BLABLABLA BLABLA 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "7349872039871087"
      val actual = StatementService.statementRowToOperation(statementRow, account, Set(cardNumber), cardRules, checkRules, debitRules, transferRules)
      actual.left.value shouldBe a [NoSuchElementException]
    }

    "when no rules matches the row" in {
      val statementRow = Statement.Row(2024 \ 3 \ 14, 2024 \ 3 \ 14, "CAAAARTE X1234 BLABLABLA BLABLA 13/03", 550.cents, 0.cents)
      val operationId = UUID.randomUUID()
      implicit val operationIdGenerator: () => Operation.Id = () => operationId
      val cardNumber = "8971298379811234"
      val actual = StatementService.statementRowToOperation(statementRow, account, Set(cardNumber), cardRules, checkRules, debitRules, transferRules)
      actual.left.value shouldBe a [MatchError]
    }

  }
}

object StatementServiceTest {
  val account: Iban = Iban("FR", "76", "1234567891011").get
  
  val cardRules: Seq[Regex] = Seq(
    raw"PAIEMENT PAR CARTE X(?<cardSuffix>\p{Digit}{4}) .* (?<incompleteDate>\p{Digit}{2}/\p{Digit}{2})\p{Space}*",
    raw"RETRAIT AU DISTRIBUTEUR X(?<cardSuffix>\p{Digit}{4}) .* (?<incompleteDate>\p{Digit}{2}/\p{Digit}{2}) \p{Digit}{2}H\p{Digit}{2}\p{Space}*"
  )
    .map(_.r)

  val checkRules: Seq[Regex] = Seq(
    raw"CHEQUE EMIS\p{Space}(?<checkNumber>\p{Digit}{7})"
  )
    .map(_.r)
  
  val debitRules: Seq[Regex] = Seq(
    raw"PRELEVEMENT\p{Space}.*",
    raw"REMBOURSEMENT DE PRET\p{Space}.*",
    raw"REGLEMENT ASSU\. CAAE PRET HABITAT\p{Space}.*"
  )
    .map(_.r)
  
  val transferRules: Seq[Regex] = Seq(
    raw"VIREMENT EMIS\p{Space}.*",
    raw"VIREMENT EN VOTRE FAVEUR\p{Space}.*"
  )
    .map(_.r)
}