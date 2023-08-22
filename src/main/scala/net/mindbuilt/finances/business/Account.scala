package net.mindbuilt.finances.business

case class Account(
  bank: Bic,
  iban: Iban,
  domiciliation: String,
  holder: Holder
) {
  def individualHolders: Set[Holder.Single] =
    holder match {
      case single: Holder.Single => Set(single)
      case multiple: Holder.Multiple => multiple.individuals
    }
}