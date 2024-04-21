package net.mindbuilt.finances

import cats.data.EitherT
import cats.{Applicative, Monad}

import scala.annotation.nowarn
import scala.language.implicitConversions
import scala.util.Try
import scala.util.matching.Regex

object Helpers {
  
  implicit class ExtendedSeqOfEitherT[F[_], A, B](seqOfEitherT: Seq[EitherT[F, A, B]]) {
    def traverse(implicit monad: Monad[F]): EitherT[F, A, Seq[B]] =
      seqOfEitherT.foldLeft(EitherT.pure[F, A](Seq.empty[B])) { (eitherTOfSeq, eitherTOfElement) =>
        eitherTOfSeq.flatMap(seq => eitherTOfElement.map(element => seq :+ element))
      }
  }
  
  implicit class ExtendedSeqOfEither[A, B](seqOfEither: Seq[Either[A, B]]) {
    def traverse: Either[A, Seq[B]] = seqOfEither.foldLeft(Either.right[A](Seq.empty[B])){ (eitherOfSeq, eitherOfElement) =>
      eitherOfSeq.flatMap(seq => eitherOfElement.map(element => seq :+ element))
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

  implicit class ExtendedRegex(regex: Regex) {
    val partialFindFirstMatchIn: PartialFunction[CharSequence, Regex.Match] = ((source: CharSequence) => regex.findFirstMatchIn(source)).unlift
  }
  
  implicit class ExtendedRegexMatch(regexMatch: Regex.Match) {
    def groupTry(id: String): Try[String] = Try {
      regexMatch.group(id)
    }
    def groupOption(id: String): Option[String] = groupTry(id).toOption
    def groupEither(id: String): Either[Throwable, String] = groupTry(id).toEither
    def groupEitherT[F[_] : Applicative](id: String): EitherT[F, Throwable, String] = EitherT.fromEither[F](groupEither(id))
  }
}
