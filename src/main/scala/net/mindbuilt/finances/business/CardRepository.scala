package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait CardRepository {
  def getByNumber(number: Card.Number): EitherT[IO, Throwable, Option[Card]]
}
