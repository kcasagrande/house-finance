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
    label: String,
    credit: Cents,
    accountDate: LocalDate,
    valueDate: LocalDate,
    `type`: Option[Class[_ <: Operation]] = None,
    reference: Option[String] = None,
    operationDate: Option[LocalDate] = None,
    cardSuffix: Option[String] = None,
    checkNumber: Option[String] = None
  )
}
