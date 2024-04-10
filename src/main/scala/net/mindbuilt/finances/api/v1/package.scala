package net.mindbuilt.finances.api

import io.circe.Encoder
import net.mindbuilt.finances.Cents

package object v1 {
  implicit val centsEncoder: Encoder[Cents] = Encoder.encodeInt.contramap(_.value)
}
