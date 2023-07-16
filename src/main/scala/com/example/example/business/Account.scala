package com.example.example.business

case class Account(
  iban: Iban,
  domiciliation: String,
  holder: Holder.Id
)