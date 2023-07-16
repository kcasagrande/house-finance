package com.example.example.business

import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers._

class BicTest
  extends AnyFreeSpecLike
    with TryValues
{
  "Bic" - {
    "apply" - {
      "should succeed when each part fulfills the expected conditions" in {
        val actual = Bic("ABCD", "FR", "A1", "ABC")
        actual.success.value shouldBe a[Bic]
      }

      "should fail when the institution code does not match the expected format" in {
        val actual = Bic("XXXXX", "FR", "A1")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }
      
      "should fail when the country code does not match the expected format" in {
        val actual = Bic("ABCD", "XXXXX", "A1")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the country code does not exist" in {
        val actual = Bic("ABCD", "XX", "A1")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the location code does not match the expected format" in {
        val actual = Bic("ABCD", "FR", "XXXXX")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }

      "should fail when the branch code does not match the expected format" in {
        val actual = Bic("ABCD", "FR", "A1", "XXXXX")
        actual.failure.exception shouldBe an[IllegalArgumentException]
      }
    }
  }
}
