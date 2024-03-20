package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait CardRepository {
  def getAll: EitherT[IO, Throwable, Set[Card]]
  def getByNumber(number: Card.Number): EitherT[IO, Throwable, Option[Card]]
  def getByAccount(account: Iban): EitherT[IO, Throwable, Set[Card]]
  def save(card: Card): EitherT[IO, Throwable, Unit]
}
