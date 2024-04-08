package net.mindbuilt.finances.api.v1

import cats.effect.IO
import io.circe.Encoder
import io.circe.literal._
import io.circe.syntax._
import net.mindbuilt.finances.api.v1.AccountController._
import net.mindbuilt.finances.application.AccountService
import net.mindbuilt.finances.business.{Account, Holder, Iban}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.io._

class AccountController(
  accountService: AccountService
) extends CirceEntityEncoder
{
  def apply(): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      accountService.getAllAccounts
        .value
        .flatMap {
          case Left(throwable) => InternalServerError(throwable.getMessage)
          case Right(accounts) => Ok(accounts.map(_.asJson))
        }
        
    case GET -> Root / IbanVar(account) / "holders" =>
      accountService.getHoldersByAccount(account)
        .value
        .flatMap {
          case Left(throwable) => InternalServerError(throwable.getMessage)
          case Right(holders) => Ok(holders.map(_.asJson))
        }
  }

}

object AccountController {
  object IbanVar {
    def unapply(string: String): Option[Iban] =
      Some(string)
        .filter(_.nonEmpty)
        .flatMap(Iban.fromString(_).toOption)
  }
  
  implicit val accountEncoder: Encoder[Account] = Encoder.instance {
    (account: Account) =>
      json"""{
                "iban": ${account.iban.toString},
                "bic": ${account.bank.toString},
                "domiciliation": ${account.domiciliation},
                "holder": ${account.holder.name}
                
          }"""
  }
  
  implicit val holderEncoder: Encoder[Holder.Single] = Encoder.instance {
    (holder: Holder.Single) =>
      json"""{
               "id": ${holder.id.toString},
               "name": ${holder.name}
             }"""
  }
}