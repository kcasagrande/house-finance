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
}
