package com.example.example.sqlite3

import anorm.SqlParser._
import anorm._
import cats.effect.IO
import com.example.example.business.Operation
import com.example.example.sqlite3.OperationRepository.operationParser
import com.example.example.{business => port}

import java.sql.Connection
import java.util.UUID

class OperationRepository(implicit val connection: Connection)
  extends port.OperationRepository
{
  override def getById(id: UUID): IO[Option[Operation]] = {
    IO(
      SQL("""SELECT * FROM operation WHERE id={id}""")
        .on("id" -> id.toString)
        .as[Option[Operation]](operationParser.singleOpt)
    )
  }

  override def save(operation: Operation): IO[Unit] = ???
}

object OperationRepository {
  private def uuid(columnName: String): RowParser[UUID] = str(columnName).map(UUID.fromString)
  
  val operationParser: RowParser[Operation] = for {
    typ <- str("type")
    id <- uuid("id")
    label <- str("label")
  } yield {
    typ match {
      case "card" => Operation.ByCard(id, label, ???, ???, ???, ???)
      case "check" => Operation.ByCheck(id, ???, label, ???, ???, ???, ???)
      case "debit" => Operation.ByDebit(id, ???, label, ???, ???, ???, ???)
      case "transfer" => Operation.ByTransfer(id, ???, ???, label, ???, ???, ???, ???)
    }
  }
}
