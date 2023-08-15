package net.mindbuilt.finances.front

import com.softwaremill.macwire.wire

trait Module {
  lazy val frontController: FrontController = wire[FrontController]
}
