package net.mindbuilt.finances

import cats.Monad
import cats.data.EitherT

import scala.annotation.nowarn

object Helpers {
  
  implicit class ExtendedSeqOfEitherT[F[_], A, B](seqOfEitherT: Seq[EitherT[F, A, B]]) {
    def traverse(implicit monad: Monad[F]): EitherT[F, A, Seq[B]] =
      seqOfEitherT.foldLeft(EitherT.pure[F, A](Seq.empty[B])) { (eitherTOfSeq, eitherTOfElement) =>
        eitherTOfSeq.flatMap(seq => eitherTOfElement.map(element => seq :+ element))
      }
  }

  implicit class ExtendedEither(either: Either.type) {
    def left[B]: ExtendedEither.LeftPartiallyApplied[B] = new ExtendedEither.LeftPartiallyApplied[B]

    def right[A]: ExtendedEither.RightPartiallyApplied[A] = new ExtendedEither.RightPartiallyApplied[A]
  }

  object ExtendedEither {
    class RightPartiallyApplied[A](
      @nowarn
      private val dummy: Boolean = true
    )
      extends AnyVal {
      def apply[B](value: B): Either[A, B] = Right[A, B](value)
    }

    class LeftPartiallyApplied[B](
      @nowarn
      private val dummy: Boolean = true
    )
      extends AnyVal {
      def apply[A](value: A): Either[A, B] = Left[A, B](value)
    }
  }


}
