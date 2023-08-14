package net.mindbuilt.finances.application

import com.softwaremill.macwire.wire
import net.mindbuilt.finances.business.OperationRepository

trait Module
{
  lazy val operationService: OperationService = wire[OperationService]
  def operationRepository: OperationRepository
}
