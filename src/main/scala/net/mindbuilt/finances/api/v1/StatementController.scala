package net.mindbuilt.finances.api.v1

import cats.data.EitherT
import cats.effect.IO
import fs2.Pipe
import fs2.text.{char2string, decodeWithCharset, lines, string2char}
import io.circe.literal.JsonStringContext
import io.circe.{Encoder, Json}
import net.mindbuilt.finances.api.v1.OperationController.operationDecoder
import net.mindbuilt.finances.api.v1.StatementController._
import net.mindbuilt.finances.application.StatementService
import net.mindbuilt.finances.business
import net.mindbuilt.finances.business.{Operation, Statement}
import org.http4s.circe.jsonOf
import org.http4s.dsl.io._
import org.http4s.{DecodeFailure, DecodeResult, EntityDecoder, HttpRoutes, InvalidMessageBodyFailure, ParseFailure, QueryParamDecoder}

import java.nio.charset.Charset

class StatementController(
  statementService: StatementService
) {
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root :? Iban(account) =>
      req.decode[fs2.Stream[IO, String]] { stream =>
        statementService.parse(account, stream).toJsonResponse()
      }
      
    case req @ POST -> Root =>
      req.decode[Seq[Operation]](statementService.`import`(_).toEmptyResponse())
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
      
  implicit val parsedRowMethodEncoder: Encoder[Class[_ <: Operation]] = Encoder.instance {
    case t if t == classOf[Operation.ByCard] => Json.fromString("card")
    case t if t == classOf[Operation.ByCheck] => Json.fromString("check")
    case t if t == classOf[Operation.ByDebit] => Json.fromString("debit")
    case t if t == classOf[Operation.ByTransfer] => Json.fromString("transfer")
  }
  
  implicit val statementFileDecoder: EntityDecoder[IO, fs2.Stream[IO, String]] =
    EntityDecoder.multipart(IO.asyncForIO).flatMapR { _
      .parts
      .find(part => part.name.contains("statement") && part.filename.nonEmpty)
      .map(_.body)
      .map(_.through(clean))
      .fold[DecodeResult[IO, fs2.Stream[IO, String]]](
        EitherT.leftT[IO, fs2.Stream[IO, String]](InvalidMessageBodyFailure("No statement file provided"))
      )(
        EitherT.pure[IO, DecodeFailure](_)
      )
    }
    
  implicit val statementOperationsDecoder: EntityDecoder[IO, Seq[Operation]] = jsonOf[IO, Seq[Operation]]

  implicit val parsedRowEncoder: Encoder[Statement.ParsedRow] = Encoder.instance { (row: Statement.ParsedRow) =>
    json"""{
      "id": ${row.id},
      "reference": ${row.reference},
      "method": ${row.method},
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