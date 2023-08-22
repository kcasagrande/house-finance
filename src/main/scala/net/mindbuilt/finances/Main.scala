package net.mindbuilt.finances

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, ErrorAction, ErrorHandling}

object Main
  extends IOApp
    with Module
    with api.v1.Module
    with application.Module
    with sqlite3.Module
{
  private def withErrorLogging(httpApp: HttpApp[IO]) =
    ErrorHandling.Recover.total(
      ErrorAction.log(
        httpApp,
        messageFailureLogAction = (throwable, message) =>
          IO.println(message) >>
          IO.println(throwable),
        serviceErrorLogAction = (throwable, message) =>
          IO.println(message) >>
          IO.println(throwable)
      )
    )
    
  private val apiRoot = "/api/v1"
  
  override def run(args: List[String]): IO[ExitCode] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"127.0.0.1")
    .withPort(port"8080")
    .withHttpApp(CORS.policy.withAllowOriginAll(withErrorLogging(Router(
      apiRoot + "/operations" -> operationController(),
      apiRoot + "/accounts" -> accountController()
    ).orNotFound)))
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)
}
