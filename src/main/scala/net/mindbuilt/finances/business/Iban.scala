package net.mindbuilt.finances.business

import java.util.Locale.IsoCountryCode.PART1_ALPHA2
import java.util.Locale.getISOCountries
import scala.util.Try

case class Iban private(
  countryCode: String,
  checkDigits: String,
  bban: String
)

object Iban {
  def fromString(ibanAsString: String): Try[Iban] =
    for {
      countryCode <- Try { ibanAsString.substring(0, 2) }
      checkDigits <- Try { ibanAsString.substring(2, 4) }
      bban <- Try { ibanAsString.substring(4) }
      iban <- Iban(countryCode, checkDigits, bban)
    } yield iban
    
  
  def apply(countryCode: String, checkDigits: String, bban: String): Try[Iban] =
    for {
      _ <- countryCode.matches("^\\p{Alpha}{2}$") orFailWith new IllegalArgumentException(s"""Country code should be 2 letters, was "$countryCode".""")
      _ <- getISOCountries(PART1_ALPHA2).contains(countryCode) orFailWith new IllegalArgumentException(s"""Country code should be an ISO3166 country code, was "$countryCode".""")
      _ <- checkDigits.matches("^\\p{Digit}{2}$") orFailWith new IllegalArgumentException(s"""Check digits should be 2 digits, was "$checkDigits".""")
      _ <- bban.matches("^\\p{Alnum}{1,30}$") orFailWith new IllegalArgumentException(s"""BBAN should be 1 to 30 alphanumeric characters, was "$bban".""")
    } yield {
      new Iban(countryCode, checkDigits, bban)
    }
}