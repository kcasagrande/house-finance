package net.mindbuilt.finances.api.v1

import cats.effect.IO
import fs2.Pipe
import fs2.text.{char2string, decodeWithCharset, lines, string2char}
import io.circe.{Encoder, Json}
import io.circe.literal.JsonStringContext
import net.mindbuilt.finances.api.v1.OperationController.operationEncoder
import net.mindbuilt.finances.api.v1.StatementController._
import net.mindbuilt.finances.application.StatementService
import net.mindbuilt.finances.business
import net.mindbuilt.finances.business.{Operation, Statement}
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}

import java.nio.charset.Charset

class StatementController(
  statementService: StatementService
) {
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root :? Iban(account) =>
      req.decode[Multipart[IO]] {
        _.parts
          .find(part => part.name.contains("statement") && part.filename.nonEmpty) match {
          case None => BadRequest("No statement file provided")
          case Some(part) =>
            statementService.parse(account, part.body.through(clean))
              .toResponse
        }
      }
 }
}

object StatementController {
  
  implicit val ibanQueryParamDecoder: QueryParamDecoder[business.Iban] = QueryParamDecoder[String]
    .emap(business.Iban.fromString(_)
      .toEither
      .left.map(throwable => ParseFailure(throwable.getMessage, throwable.getStackTrace.map(_.toString).mkString("\n")))
    )
    
  private object Iban extends QueryParamDecoderMatcher[business.Iban]("account")
  
  val clean: Pipe[IO, Byte, String] =
    decodeWithCharset[IO](Charset.forName("Windows-1252"))
      .andThen(lines)
      .andThen(_.drop(10))
      .andThen(_.map(_ + "\n"))
      .andThen(string2char[IO])
      .andThen(char2string[IO])
      
  implicit val parsedRowTypeEncoder: Encoder[Class[_ <: Operation]] = Encoder.instance {
    case t if t == classOf[Operation.ByCard] => Json.fromString("card")
    case t if t == classOf[Operation.ByCheck] => Json.fromString("check")
    case t if t == classOf[Operation.ByDebit] => Json.fromString("debit")
    case t if t == classOf[Operation.ByTransfer] => Json.fromString("transfer")
  }
  
  implicit val parsedRowEncoder: Encoder[Statement.ParsedRow] = Encoder.instance { (row: Statement.ParsedRow) =>
    json"""{
      "type": ${row.`type`},
      "reference": ${row.reference},
      "label": ${row.label},
      "credit": ${row.credit},
      "accountDate": ${row.accountDate},
      "valueDate": ${row.valueDate},
      "operationDate": ${row.operationDate},
      "card": ${row.card.map(_.number)},
      "checkNumber": ${row.checkNumber}
    }""".dropNullValues
  }
}