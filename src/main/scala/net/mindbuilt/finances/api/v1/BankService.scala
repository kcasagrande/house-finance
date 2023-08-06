package net.mindbuilt.finances.api.v1

import cats.effect.IO
import io.circe._
import io.circe.literal._
import net.mindbuilt.finances.business.{Bank, Bic}
import net.mindbuilt.finances.sqlite3.BankRepository
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}

import java.sql.Connection

class BankService(implicit connection: Connection) {
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      implicit val encoder: EntityEncoder[IO, Option[Bank]] = jsonEncoderOf(Encoder.instance[Option[Bank]] {
        case None => Json.Null
        case Some(Bank(bic, designation)) => json"""{"bic": ${bic.toString}, "designation": $designation}"""
      })
      val repository = new BankRepository()(???)
      repository.getByBic(Bic("AGRI", "FR", "PP", "878").get)
        .getOrElse(None)
        .flatMap(Ok(_))
  }
}
