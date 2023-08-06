package net.mindbuilt.finances

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import net.mindbuilt.finances.api.v1.BankService
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}

import java.sql.{Connection, DriverManager}

object Main
  extends IOApp
{
  implicit val connection: Connection = {
    val connection = DriverManager.getConnection("jdbc:sqlite:file:/home/orion/finances/finances.sqlite3?mode=ro")
    connection.setAutoCommit(false)
    connection
  }
  
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
  override def run(args: List[String]): IO[ExitCode] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"127.0.0.1")
    .withPort(port"8080")
    .withHttpApp(withErrorLogging(Router(
      "/banks" -> new BankService().apply()
    ).orNotFound))
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)
}
