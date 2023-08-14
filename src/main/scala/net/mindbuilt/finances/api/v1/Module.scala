package net.mindbuilt.finances.api.v1

import com.softwaremill.macwire.wire
import net.mindbuilt.finances.application.OperationService

trait Module
{
  lazy val operationController: OperationController = wire[OperationController]
  def operationService: OperationService
}
