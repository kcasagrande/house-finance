package com.example.example.bank.sample

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.io.file.Path
import net.mindbuilt.finances.IntToCents
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.time.LocalDate
import scala.language.postfixOps

class CsvReaderTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
{
  "CsvReader.apply" - {

    "should load a file with no entries" in {
      val path = Path("src/test/resources/sample-bank/empty.csv")
      val entries: IO[Seq[Operation]] = CsvReader(path).compile.toList
      entries.asserting(_ shouldBe empty)
    }

    "should load a file with one entry" in {
      val path = Path("src/test/resources/sample-bank/one.csv")
      val entries: IO[Seq[Operation]] = CsvReader(path).compile.toList
      entries.asserting(_ should contain theSameElementsAs Seq(
        Operation(
          LocalDate.of(2023, 1, 17),
          LocalDate.of(2023, 1, 18),
          """XXXXXXXXXX
            |XXXXX XXXXXXXXX  
            |
            |
            |""".stripMargin,
          10 euros,
          0 euros
        )
      ))
    }

    "should load a file with many entries" in {
      val path = Path("src/test/resources/sample-bank/many.csv")
      val entries: IO[Seq[Operation]] = CsvReader(path).compile.toList
      entries.asserting(_ should contain theSameElementsAs Seq(
        Operation(
          LocalDate.of(2023, 1, 17),
          LocalDate.of(2023, 1, 18),
          """XXXXXXXXXX
            |XXXXX XXXXXXXXX  
            |
            |
            |""".stripMargin,
          10 euros,
          0 euros
        ),
        Operation(
          LocalDate.of(2023, 1, 10),
          LocalDate.of(2023, 1, 10),
          """XXXXXXXXXXXXXXXXXXXXX
            |XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  
            |
            |
            |""".stripMargin,
          20011 cents,
          0 euros
        )
      ))
    }

  }

}
