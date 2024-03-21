package net.mindbuilt.finances

import cats.Monad
import cats.data.EitherT

object Helpers {
  
  implicit class ExtendedListOfEitherT[F[_] : Monad, A, B](listOfEitherT: List[EitherT[F, A, B]]) {
    def traverse: EitherT[F, A, List[B]] =
      listOfEitherT.foldLeft(EitherT.pure[F, A](List.empty[B])) { (eitherTOfList, eitherTOfElement) =>
        eitherTOfList.flatMap(list => eitherTOfElement.map(element => list :+ element))
      }
  }

}
