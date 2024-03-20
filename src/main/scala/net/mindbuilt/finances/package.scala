package net.mindbuilt

package object finances
{
  case class Cents(value: Int) extends AnyVal {
    def +(other: Cents): Cents = Cents(value + other.value)
    def -(other: Cents): Cents = Cents(value - other.value)
  }
  implicit class IntToCents(value: Int) {
    def cents: Cents = Cents(value)
    def euros: Cents = Cents(value * 100)
  }
}
