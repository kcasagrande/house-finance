package net.mindbuilt.finances.sqlite3

import anorm.SqlParser.long
import cats.data.EitherT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._
import org.sqlite.{SQLiteErrorCode, SQLiteException}

import java.sql.Connection

class Sqlite3Test
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
    with InMemoryDatabase
{
  "executeBatchWithEffect" - {
    "should return a failure when the table structure is invalid" in {
      val result = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withConnection { implicit connection: Connection =>
          executeBatchWithEffect(
            """INSERT INTO "invalid_table"("invalid_column") VALUES({value})""",
            namedParameters("value" -> 0),
            namedParameters("value" -> 1)
          )
        }
      }
      result.value.asserting(_.left.value.asInstanceOf[SQLiteException].getResultCode shouldEqual SQLiteErrorCode.SQLITE_ERROR)
    }
    
    "should return a failure when there is a unique constraint violation" in {
      val result = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withConnection { implicit connection: Connection =>
          executeBatchWithEffect(
            """INSERT INTO "card_type"("name") VALUES({name})""",
            namedParameters("name" -> "VISA"),
            namedParameters("name" -> "VISA")
          )
        }
      }
      result.value.asserting(_.left.value.asInstanceOf[SQLiteException].getResultCode shouldEqual SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY)
    }
  }
  
  "executeQueryWithEffect" - {
    "should return a failure" in {
      val result = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withConnection { implicit connection: Connection =>
          executeQueryWithEffect("""SELECT COUNT(*) AS "count" FROM "invalid_table"""")(long("count").single)
        }
      }
      result.value.asserting(_.left.value.asInstanceOf[SQLiteException].getResultCode shouldEqual SQLiteErrorCode.SQLITE_ERROR)
    }
  }

  "executeWithEffect" - {
    "should return a failure" in {
      val result = withDatabase { implicit database: EitherT[IO, Throwable, Database] =>
        withConnection { implicit connection: Connection =>
          executeWithEffect("""INSERT INTO "invalid_table"("invalid_column") VALUES(0)""")
        }
      }
      result.value.asserting(_.left.value.asInstanceOf[SQLiteException].getResultCode shouldEqual SQLiteErrorCode.SQLITE_ERROR)
    }
  }
}
