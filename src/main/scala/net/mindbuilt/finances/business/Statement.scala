package net.mindbuilt.finances.business

import net.mindbuilt.finances.Cents

import java.time.LocalDate

object Statement {
  case class Row(
    date: LocalDate,
    dateValeur: LocalDate,
    libelle: String,
    debit: Cents,
    credit: Cents
  )

  case class ParsedRow(
    reference: String,
    label: String,
    credit: Cents,
    accountDate: LocalDate,
    valueDate: LocalDate,
    method: Option[Class[_ <: Operation]] = None,
    operationDate: Option[LocalDate] = None,
    card: Option[Card] = None,
    checkNumber: Option[String] = None
  )
}
