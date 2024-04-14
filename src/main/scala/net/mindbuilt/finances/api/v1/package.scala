package net.mindbuilt.finances.api

import cats.data.EitherT
import cats.effect.IO
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import net.mindbuilt.finances.Cents
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
}
