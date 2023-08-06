package net.mindbuilt.finances.sqlite3

import anorm.SqlParser.scalar
import anorm._
import cats.data.EitherT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.EitherValues
import org.scalatest.freespec.AsyncFreeSpecLike
import org.scalatest.matchers.should.Matchers._

import java.sql.{Connection, DriverManager}
import java.util.UUID
import scala.util.Try

class packageTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with EitherValues
{
  "withTransaction" - {
    "should not save data when something wrong happens" in {
      val databaseUrl = s"jdbc:sqlite:file:memory-${UUID.randomUUID().toString}?mode=memory&cache=shared"
      val result = for {
        connection <- EitherT(IO.delay(Try { DriverManager.getConnection(databaseUrl) }.toEither))
        _ <- EitherT.liftF(IO.delay(SQL("""CREATE TABLE "test"("id" NUMBER NOT NULL PRIMARY KEY)""").execute()(connection)))
        _ <- withTransaction[Unit] { implicit connection: Connection =>
          for {
            _ <- EitherT(IO.delay(Try { SQL("""INSERT INTO "test"("id") VALUES (1)""").executeUpdate() }.toEither))
            _ <- EitherT(IO.delay(Try { SQL("""INSERT INTO "test"("id") VALUES (1)""").executeUpdate() }.recover(_ => Right(())).toEither))
          } yield {
            ()
          }
        }(connection)
        number <- EitherT.liftF[IO, Throwable, Int](IO.delay(SQL("""SELECT COUNT(*) FROM "test"""").executeQuery()(connection).as(scalar[Int].single)(connection)))
      } yield {
        number
      }
      result.value.asserting(_.value shouldEqual 0)
    }
  }
}
