package com.example.example.business

import cats.effect.IO

import java.util.UUID

trait OperationRepository
{
  def getById(id: UUID): IO[Option[Operation]]
  def save(operation: Operation): IO[Unit]
}
