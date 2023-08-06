package net.mindbuilt.finances.business

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
  type Type = String
}