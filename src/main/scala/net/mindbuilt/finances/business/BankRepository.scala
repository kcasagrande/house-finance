package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait BankRepository
{
  def getAll: EitherT[IO, Throwable, Set[Bank]]
  def getByBic(bic: Bic): EitherT[IO, Throwable, Option[Bank]]
  def save(bank: Bank): EitherT[IO, Throwable, Unit]
}
