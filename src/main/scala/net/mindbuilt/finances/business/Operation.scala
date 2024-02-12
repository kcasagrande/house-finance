package net.mindbuilt.finances.business

import net.mindbuilt.finances.Cents

import java.time.LocalDate
import java.util.UUID

sealed trait Operation extends Product {
  def id: Operation.Id
  def label: String
  def credit: Cents = Cents(breakdown.map(_.credit.value).sum)
  def operationDate: LocalDate
  def valueDate: LocalDate
  def accountDate: LocalDate
  def breakdown: Seq[Operation.Breakdown]
  def withBreakdown(breakdown: Seq[Operation.Breakdown]): Operation
}

object Operation {
  type Id = UUID
  
  case class ByCard(
    override val id: Operation.Id,
    card: Card.Number,
    reference: Option[String],
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    override val breakdown: Seq[Breakdown]
  ) extends Operation {
    override def withBreakdown(breakdown: Seq[Breakdown]): ByCard = this.copy(breakdown = breakdown)
  }
  
  object ByCard {
    def apply(
      id: Operation.Id,
      card: Card.Number,
      reference: Option[String],
      label: String,
      credit: Cents,
      operationDate: LocalDate,
      valueDate: LocalDate,
      accountDate: LocalDate
    ): Operation.ByCard =
      Operation.ByCard(id, card, reference, label, operationDate, valueDate, accountDate, Seq(Breakdown(credit)))
  }
  
  case class ByCheck(
    override val id: Operation.Id,
    account: Iban,
    number: String,
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    override val breakdown: Seq[Breakdown]
  ) extends Operation {
    override def withBreakdown(breakdown: Seq[Breakdown]): ByCheck = this.copy(breakdown = breakdown)
  }
  
  object ByCheck {
    def apply(
      id: Operation.Id,
      account: Iban,
      number: String,
      label: String,
      credit: Cents,
      operationDate: LocalDate,
      valueDate: LocalDate,
      accountDate: LocalDate
    ): Operation.ByCheck =
      Operation.ByCheck(id, account, number, label, operationDate, valueDate, accountDate, Seq(Breakdown(credit)))
  }

  case class ByDebit(
    override val id: Operation.Id,
    account: Iban,
    reference: Option[String],
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    override val breakdown: Seq[Breakdown]
  ) extends Operation {
    override def withBreakdown(breakdown: Seq[Breakdown]): ByDebit = this.copy(breakdown = breakdown)
  }
  
  object ByDebit {
    def apply(
      id: Operation.Id,
      account: Iban,
      reference: Option[String],
      label: String,
      credit: Cents,
      operationDate: LocalDate,
      valueDate: LocalDate,
      accountDate: LocalDate
    ): Operation.ByDebit =
      Operation.ByDebit(id, account, reference, label, operationDate, valueDate, accountDate, Seq(Breakdown(credit)))
  }

  case class ByTransfer(
    override val id: Operation.Id,
    account: Iban,
    reference: Option[String],
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    otherParty: Option[Iban],
    override val breakdown: Seq[Breakdown]
  ) extends Operation {
    override def withBreakdown(breakdown: Seq[Breakdown]): ByTransfer = this.copy(breakdown = breakdown)
  }
  
  object ByTransfer {
    def apply(
      id: Operation.Id,
      account: Iban,
      reference: Option[String],
      label: String,
      credit: Cents,
      operationDate: LocalDate,
      valueDate: LocalDate,
      accountDate: LocalDate,
      otherParty: Option[Iban]
    ): Operation.ByTransfer =
      Operation.ByTransfer(id, account, reference, label, operationDate, valueDate, accountDate, otherParty, Seq(Breakdown(credit)))
  }
  
  case class Breakdown(
    credit: Cents,
    category: Option[String] = None,
    comment: Option[String] = None,
    supplier: Option[Holder.Id] = None
  )
}
