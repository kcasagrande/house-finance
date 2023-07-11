package com.example

package object example
{
  case class Cents(value: Int) extends AnyVal
  implicit class IntToCents(value: Int) {
    def cents: Cents = Cents(value)
    def euros: Cents = Cents(value * 100)
  }
}
