package net.mindbuilt.finances.front

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.twirl._

class FrontController()
{
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "operations" => Ok(html.operations())
  }
}
