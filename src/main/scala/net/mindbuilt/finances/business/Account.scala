package net.mindbuilt.finances.business

case class Account(
  bank: Bic,
  iban: Iban,
  domiciliation: String,
  holder: Holder
)