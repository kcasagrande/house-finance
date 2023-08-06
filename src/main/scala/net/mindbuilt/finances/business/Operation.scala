package net.mindbuilt.finances.business

import java.time.LocalDate
import java.util.UUID

sealed trait Operation {
  def id: Operation.Id
  def label: String
  def operationDate: LocalDate
  def valueDate: LocalDate
  def accountDate: LocalDate
}

object Operation {
  type Id = UUID
  
  case class ByCard(
    override val id: UUID,
    val card: Card.Number,
    val reference: String,
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation
  
  case class ByCheck(
    override val id: UUID,
    account: Iban,
    number: String,
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation

  case class ByDebit(
    override val id: UUID,
    account: Iban,
    reference: String,
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate
  ) extends Operation

  case class ByTransfer(
    override val id: UUID,
    account: Iban,
    reference: String,
    override val label: String,
    override val operationDate: LocalDate,
    override val valueDate: LocalDate,
    override val accountDate: LocalDate,
    otherParty: Option[Iban]
  ) extends Operation
}
