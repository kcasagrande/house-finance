package net.mindbuilt.finances.business

import net.mindbuilt.finances.Cents

import java.time.LocalDate
import java.util.UUID

sealed trait Operation {
  def id: Operation.Id
  def label: String
  def credit: Cents
  def operationDate: LocalDate
  def valueDate: LocalDate
  def accountDate: LocalDate
}

object Operation {
  type Id = UUID
  
  case class ByCard(
    override val id: Operation.Id,
    val card: Card.Number,
    val reference: String,
    override val label: String,
    override val credit: Cents,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation
  
  case class ByCheck(
    override val id: Operation.Id,
    account: Iban,
    number: String,
    override val label: String,
    override val credit: Cents,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation

  case class ByDebit(
    override val id: Operation.Id,
    account: Iban,
    reference: String,
    override val label: String,
    override val credit: Cents,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation

  case class ByTransfer(
    override val id: Operation.Id,
    account: Iban,
    reference: String,
    override val label: String,
    override val credit: Cents,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    otherParty: Option[Iban]
  ) extends Operation
}
