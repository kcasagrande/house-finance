package net.mindbuilt.finances

import cats.Monad
import cats.data.EitherT

object Helpers {
  
  implicit class ExtendedSeqOfEitherT[F[_], A, B](seqOfEitherT: Seq[EitherT[F, A, B]]) {
    def traverse(implicit monad: Monad[F]): EitherT[F, A, Seq[B]] =
      seqOfEitherT.foldLeft(EitherT.pure[F, A](Seq.empty[B])) { (eitherTOfSeq, eitherTOfElement) =>
        eitherTOfSeq.flatMap(seq => eitherTOfElement.map(element => seq :+ element))
      }
  }

}
