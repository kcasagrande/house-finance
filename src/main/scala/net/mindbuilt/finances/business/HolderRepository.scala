package net.mindbuilt.finances.business

import cats.data.EitherT
import cats.effect.IO

trait HolderRepository {
  def getAllIndividuals: EitherT[IO, Throwable, Set[Holder.Single]]
  def getById(id: Holder.Id): EitherT[IO, Throwable, Option[Holder.Single]]
}
