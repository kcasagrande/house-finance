package net.mindbuilt.finances.application

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{AccountRepository, Holder, Iban}

class AccountService(
  accountRepository: AccountRepository
) {
  def getHoldersByAccount(account: Iban): EitherT[IO, Throwable, Set[Holder.Single]] =
    accountRepository.getByIban(account).map(_.map(_.individualHolders).getOrElse(Set.empty[Holder.Single]))
}
