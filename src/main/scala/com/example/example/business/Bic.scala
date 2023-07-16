package com.example.example.business

import java.util.Locale.IsoCountryCode.PART1_ALPHA2
import java.util.Locale.getISOCountries
import scala.util.Try

case class Bic private(
  institutionCode: String,
  countryCode: String,
  locationCode: String,
  branchCode: String
)

object Bic {
  def apply(institutionCode: String, countryCode: String, locationCode: String, branchCode: String = "XXX"): Try[Bic] =
  for {
    _ <- institutionCode.matches("^\\p{Alpha}{4}$") orFailWith new IllegalArgumentException(s"""Institution code should be 4 letters, was "$institutionCode".""")
    _ <- countryCode.matches("^\\p{Alpha}{2}$") orFailWith new IllegalArgumentException(s"""Country code should be 2 letters, was "$countryCode".""")
    _ <- getISOCountries(PART1_ALPHA2).contains(countryCode) orFailWith new IllegalArgumentException(s"""Country code should be an ISO3166 country code, was "$countryCode".""")
    _ <- locationCode.matches("^\\p{Alnum}{2}$") orFailWith new IllegalArgumentException(s"""Location code should be 2 alphanumeric characters, was "$locationCode".""")
    _ <- branchCode.matches("^\\p{Alnum}{3}$") orFailWith new IllegalArgumentException(s"""Branch code should be 3 alphanumeric characters, was "$branchCode".""")
  } yield {
    new Bic(institutionCode, countryCode, locationCode, branchCode)
  }
}
