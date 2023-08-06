package net.mindbuilt.finances.business

import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers._

class IbanTest
  extends AnyFreeSpecLike
    with TryValues
{
  "Iban" - {
    "apply" - {
      "should succeed when each part fulfills the expected conditions" in {
        val actual = Iban("FR", "12", "1234567890ABCDEFGHIJKLMNOPQRST")
        actual.success.value shouldBe an[Iban]
      }

      "should fail when the country code does not match the expected format" in {
        val actual = Iban("XXXXX", "12", "1234567890ABCDEFGHIJKLMNOPQRST")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the country code does not exist" in {
        val actual = Iban("XX", "12", "1234567890ABCDEFGHIJKLMNOPQRST")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the check digits do not match the expected format" in {
        val actual = Iban("FR", "XX", "1234567890ABCDEFGHIJKLMNOPQRST")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the BBAN is empty" in {
        val actual = Iban("FR", "12", "")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the BBAN does not match the expected format" in {
        val actual = Iban("FR", "12", "12345678901234567890123456789000000")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }
    }
  }
}
