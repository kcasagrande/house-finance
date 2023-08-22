package net.mindbuilt.finances.business

import net.mindbuilt.finances.business.Holder.Multiple.Combination

import java.util.UUID

sealed trait Holder {
  def name: String
}

object Holder {
  type Id = UUID
  
  case class Single(id: Holder.Id, name: String) extends Holder
  
  case class Multiple(combination: Combination, individuals: Set[Holder.Single]) extends Holder {
    final override def name: String = individuals.toSeq.sortBy(_.name).map(_.name).mkString(s" ${combination.separator} ")
  }
  
  object Multiple {
    sealed abstract class Combination(val separator: String) {
      final override def toString: String = separator
      def of: Set[Holder.Single] => Multiple
    }
    
    def allOf(individuals: Set[Holder.Single]): Multiple = Combination.And.of(individuals)
    def oneOf(individuals: Set[Holder.Single]): Multiple = Combination.Or.of(individuals)
    
    object Combination {
      case object And extends Combination("AND") {
        override val of: Set[Holder.Single] => Multiple = (individuals: Set[Holder.Single]) => Multiple(this, individuals)
      }
      case object Or extends Combination("OR") {
        override val of: Set[Holder.Single] => Multiple = (individuals: Set[Holder.Single]) => Multiple(this, individuals)
      }
    }
  }
}