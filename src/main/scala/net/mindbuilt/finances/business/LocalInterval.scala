package net.mindbuilt.finances.business

import java.time.LocalDate

case class LocalInterval private(
  start: LocalDate,
  end: LocalDate
)

object LocalInterval {
  def apply(date1: LocalDate, date2: LocalDate): LocalInterval =
    if(date1.isBefore(date2)) {
      new LocalInterval(date1, date2)
    } else {
      new LocalInterval(date2, date1)
    }
    
  implicit class LocalIntervalBoundary(boundary: LocalDate) {
    def to(otherBoundary: LocalDate): LocalInterval = LocalInterval(boundary, otherBoundary)
  }
}