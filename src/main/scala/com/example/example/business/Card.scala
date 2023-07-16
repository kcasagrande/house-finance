package com.example.example.business

import java.time.YearMonth

case class Card(
  number: Card.Number,
  account: Iban,
  holder: Holder.Id,
  expiration: YearMonth,
  `type`: Card.Type
)

object Card {
  type Number = String
  
  sealed trait Type
  object Type {
    case object Visa extends Type
    case object Mastercard extends Type
  }
}