package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait AccountRepository {
  def getByIban(iban: Iban): EitherT[IO, Throwable, Option[Account]]
  def save(account: Account): EitherT[IO, Throwable, Unit]
}
