package com.example.example.bank.sample

import com.example.example.Cents

import java.time.LocalDate

case class Operation(
  date: LocalDate,
  dateValeur: LocalDate,
  libellé: String,
  débit: Cents,
  crédit: Cents
)