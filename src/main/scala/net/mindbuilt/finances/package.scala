package net.mindbuilt

package object finances
{
  case class Cents(value: Int) extends AnyVal
  implicit class IntToCents(value: Int) {
    def cents: Cents = Cents(value)
    def euros: Cents = Cents(value * 100)
  }
}
