package com.example.example.business

import cats.effect.IO

trait CardRepository {
  def getByNumber(number: Card.Number): IO[Option[Card]]
}
