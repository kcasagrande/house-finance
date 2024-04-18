package net.mindbuilt.finances.api

import cats.data.EitherT
import cats.effect.IO
import io.circe.literal.JsonStringContext
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import io.circe.syntax.EncoderOps
import net.mindbuilt.finances.Cents
import net.mindbuilt.finances.business.Iban
import org.http4s.Response
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.io._

package object v1 {
  implicit class ServiceResult[T](serviceResult: EitherT[IO, Throwable, T])
    extends CirceEntityEncoder
  {
    def toResponse(implicit encoder: Encoder[T]): IO[Response[IO]] =
      serviceResult
        .value
        .flatMap {
          case Left(throwable) => InternalServerError(throwable.getMessage)
          case Right(result) => Ok(result.asJson)
        }
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
  
}
