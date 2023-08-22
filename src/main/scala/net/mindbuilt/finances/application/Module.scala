package net.mindbuilt.finances.application

import com.softwaremill.macwire.wire
import net.mindbuilt.finances.business.{AccountRepository, OperationRepository}

trait Module
{
  lazy val operationService: OperationService = wire[OperationService]
  def operationRepository: OperationRepository
  lazy val accountService: AccountService = wire[AccountService]
  def accountRepository: AccountRepository
}
