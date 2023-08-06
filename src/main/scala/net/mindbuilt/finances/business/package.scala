package net.mindbuilt.finances

import scala.util.{Try, Success, Failure}

package object business
{
  implicit class VerifiableCondition(condition: => Boolean) {
    def orFailWith(throwable: Throwable): Try[Unit] =
      if (condition) {
        Success(())
      } else {
        Failure(throwable)
      }
  }
}
