package net.mindbuilt.finances.sqlite3

import anorm.SqlParser._
import anorm._
import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.Holder.Single
import net.mindbuilt.finances.business.{Holder, HolderRepository => Port}
import net.mindbuilt.finances.sqlite3.AccountRepository.singleHolder

import java.sql.Connection
import scala.language.implicitConversions

class HolderRepository(implicit val database: EitherT[IO, Throwable, Database])
  extends Port
{
  override def getAllIndividuals: EitherT[IO, Throwable, Set[Holder.Single]] =
    withConnection { implicit connection: Connection =>
        executeQueryWithEffect(
          """SELECT
            |  "h"."id",
            |  "h"."name"
            |FROM "holder" "h"
            |""".stripMargin
        )(singleHolder.set)
    }
  
  override def getById(id: Holder.Id): EitherT[IO, Throwable, Option[Holder.Single]] =
    withConnection { implicit connection: Connection =>
      executeQueryWithEffect(
        """SELECT
          |  "h"."id",
          |  "h"."name"
          |FROM "holder" "h"
          |WHERE "h"."id"={id}
          |""".stripMargin,
        namedParameters(
          "id" -> id
        ):_*
      )(singleHolder.singleOpt)
    }
}

object HolderRepository {
  val singleHolder: RowParser[Single] = for {
    id <- uuid("id")
    name <- str("name")
  } yield {
    Holder.Single(id, name)
  }
}
