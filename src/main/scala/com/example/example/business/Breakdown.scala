package com.example.example.business

import com.example.example.Cents

case class Breakdown(
  operation: Operation.Id,
  credit: Cents,
  category: String,
  comment: String,
  valueAddedTax: Cents,
  supplier: Holder.Id
)
