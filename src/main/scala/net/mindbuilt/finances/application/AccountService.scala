package net.mindbuilt.finances.application

import cats.data.EitherT
import cats.effect.IO
import net.mindbuilt.finances.business.{Account, AccountRepository, Card, CardRepository, Holder, Iban}

class AccountService(
  accountRepository: AccountRepository,
  cardRepository: CardRepository
) {
  def getAllAccounts: EitherT[IO, Throwable, Set[Account]] =
    accountRepository.getAll
    
  def getHoldersByAccount(account: Iban): EitherT[IO, Throwable, Set[Holder.Single]] =
    accountRepository.getByIban(account).map(_.map(_.individualHolders).getOrElse(Set.empty[Holder.Single]))
    
  def getCardsByAccount(account: Iban): EitherT[IO, Throwable, Set[(Card, Holder.Single)]] =
    cardRepository.getByAccountWithHolder(account)
}
