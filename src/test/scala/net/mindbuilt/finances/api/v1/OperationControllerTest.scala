package net.mindbuilt.finances.api.v1

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.circe.Json
import io.circe.Decoder.decodeJson
import net.mindbuilt.finances.application.OperationService
import net.mindbuilt.finances.sqlite3.{InMemoryDatabase, OperationRepository}
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.freespec.AsyncFreeSpecLike

class OperationControllerTest
  extends AsyncFreeSpecLike
    with AsyncIOSpec
    with InMemoryDatabase
{
  "OperationController" - {
    
    "should work" in withDatabase { implicit database =>
      val operationRepository = new OperationRepository
      val operationService = new OperationService(operationRepository)
      val operationController = new OperationController(operationService)
      val client: Client[IO] = Client.fromHttpApp(Router("/api/v1/operations" -> operationController()).orNotFound)
      val request: Request[IO] = Request(
        method = GET,
        uri = uri"/api/v1/operations?from=2020-01-01&to=2020-12-31"
      )
      val response: IO[Json] = client.expect[Json](request)(jsonOf[IO, Json])
      response
        .map { json =>
          assert(json == Json.arr())
        }
    }
    
  }

}
