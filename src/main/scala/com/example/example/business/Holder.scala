package com.example.example.business

import com.example.example.business.Holder.Multiple.Combination
import com.example.example.business.Holder.Multiple.Combination._

import java.util.UUID

sealed trait Holder {
  def id: Holder.Id
  def name: String
}

object Holder {
  sealed trait Id {
    def value: String
  }
  object Id {
    case class Individual(id: UUID) extends Id {
      override def value: String = id.toString
    }
    sealed abstract class Composite[C <: Combination](implicit combination: C) extends Id {
      def individuals: Seq[Id.Individual]
      override def value: String = individuals.mkString(s" ${combination.separator} ")
    }
    object Composite {
      case class AllOf(override val individuals: Seq[Id.Individual]) extends Composite[And.type]
      case class OneOf(override val individuals: Seq[Id.Individual]) extends Composite[Or.type]
    }
  }
  
  case class Single(id: Holder.Id.Individual, name: String) extends Holder
  
  sealed abstract class Multiple[C <: Combination](implicit combination: C) extends Holder {
    def individuals: Set[Holder.Single]
    override def id: Id.Composite[C]
    final override def name: String = individuals.toSeq.sortBy(_.name).map(_.name).mkString(s" ${combination.separator} ")
  }
  
  object Multiple {
    sealed abstract class Combination(val separator: String) {
      final override def toString: String = separator
    }
    
    object Combination {
      implicit case object And extends Combination("ET")
      implicit case object Or extends Combination("OU")
    }
    case class AllOf(individuals: Set[Holder.Single])
      extends Multiple[And.type] {
      final override def id: Id.Composite.AllOf = Id.Composite.AllOf(individuals.toSeq.sortBy(_.name).map(_.id))
    }
    case class OneOf(individuals: Set[Holder.Single])
      extends Multiple[Or.type] {
      final override def id: Id.Composite.OneOf = Id.Composite.OneOf(individuals.toSeq.sortBy(_.name).map(_.id))
    }
  }
}