package net.mindbuilt.finances.bank.sample

import net.mindbuilt.finances.Cents

import java.time.LocalDate

case class Operation(
  date: LocalDate,
  dateValeur: LocalDate,
  libellé: String,
  débit: Cents,
  crédit: Cents
)