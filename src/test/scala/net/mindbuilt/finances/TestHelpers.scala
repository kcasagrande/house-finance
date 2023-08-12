package net.mindbuilt.finances

import java.time.{LocalDate, Month, YearMonth}
import scala.language.implicitConversions

trait TestHelpers {
  implicit class LocalDateYear(year: Int) {
    def -(month: Month): YearMonth = YearMonth.of(year, month)
  }
  
  implicit class LocalDateYearMonth(yearMonth: YearMonth) {
    def -(day: Int): LocalDate = yearMonth.atDay(day)
  }
}
