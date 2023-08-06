package net.mindbuilt.finances.business

import net.mindbuilt.finances.Cents

case class Breakdown(
  operation: Operation.Id,
  credit: Cents,
  category: String,
  comment: String,
  valueAddedTax: Cents,
  supplier: Holder.Id
)
