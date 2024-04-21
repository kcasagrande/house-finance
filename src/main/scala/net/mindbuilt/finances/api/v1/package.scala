package net.mindbuilt.finances.api

import cats.data.EitherT
import cats.effect.IO
import io.circe.literal.JsonStringContext
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import net.mindbuilt.finances.Cents
import net.mindbuilt.finances.business.Iban
import org.http4s.Response
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.io._
import org.sqlite.SQLiteException

package object v1 {
  implicit class ServiceResult[T](serviceResult: EitherT[IO, Throwable, T])
    extends CirceEntityEncoder
  {
    private[this] def toResponse(resultToResponse: T => IO[Response[IO]])(onError: PartialFunction[Throwable, IO[Response[IO]]] = PartialFunction.empty[Throwable, IO[Response[IO]]]): IO[Response[IO]] =
      serviceResult
        .value
        .flatMap {
          case Left(throwable) => onError.applyOrElse(throwable, (_throwable: Throwable) => InternalServerError(_throwable.getMessage))
          case Right(result) => resultToResponse(result)
        }
    
    def toJsonResponse(onError: PartialFunction[Throwable, IO[Response[IO]]] = PartialFunction.empty[Throwable, IO[Response[IO]]])(implicit encoder: Encoder[T]): IO[Response[IO]] =
      toResponse((result: T) => Ok(result.asJson))(onError)
        
    def toEmptyResponse(onError: PartialFunction[Throwable, IO[Response[IO]]] = PartialFunction.empty[Throwable, IO[Response[IO]]]): IO[Response[IO]] =
      toResponse(_ => Ok())(onError)
  }
  
  implicit val centsEncoder: Encoder[Cents] = Encoder.encodeInt.contramap(_.value)
  implicit val centsDecoder: Decoder[Cents] = Decoder.decodeInt.map(Cents)

  implicit val ibanEncoder: Encoder[Iban] = Encoder.instance { (iban: Iban) =>
    json"""{
             "countryCode": ${iban.countryCode},
             "checkDigits": ${iban.checkDigits},
             "bban": ${iban.bban}
           }"""
  }
  implicit val ibanDecoder: Decoder[Iban] = (c: HCursor) =>
    (
      for {
        countryCode <- c.downField("countryCode").as[String]
        checkDigits <- c.downField("checkDigits").as[String]
        bban <- c.downField("bban").as[String]
      } yield {
        Iban(countryCode, checkDigits, bban)
      }
      )
      .flatMap(_.toEither)
      .left.map(DecodingFailure.fromThrowable(_, c.history))
  
  object SQLiteException {
    def unapply(sqliteException: SQLiteException): Option[(Int, String)] =
      Some(sqliteException.getErrorCode, sqliteException.getMessage)
  }
  
  val sqliteExceptionToResponse: PartialFunction[Throwable, IO[Response[IO]]] = {
    case sqliteException: SQLiteException => sqliteException.getErrorCode match {
      case SQLITE_CONSTRAINT_UNIQUE => Conflict(sqliteException.getMessage)
    }
  }
  
  val SQLITE_CONSTRAINT_UNIQUE = 19
}
