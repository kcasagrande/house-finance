package net.mindbuilt.finances.api.v1

import cats.data.EitherT
import cats.effect.IO
import fs2.Pipe
import fs2.text.{char2string, decodeWithCharset, lines, string2char}
import net.mindbuilt.finances.api.v1.StatementController.{Iban, clean}
import net.mindbuilt.finances.application.StatementService
import net.mindbuilt.finances.business
import StatementService.ExtendedEither
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.dsl.io._
import org.http4s.multipart.Multipart

import java.nio.charset.Charset

class StatementController(
  statementService: StatementService
) {
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root :? Iban(account) =>
      req.decode[Multipart[IO]] { _.parts
        .find(part => part.name.contains("statement") && part.filename.nonEmpty) match {
          case None => BadRequest("No statement file provided")
          case Some(part) =>
            statementService.`import`(account, part.body.through(clean))
              .value
              .flatMap {
                case Left(throwable) => InternalServerError(throwable.getMessage)
                case Right(importedStatements) => Ok("Imported %d statements in account %s.".format(importedStatements, account))
              }
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
      
}