package pl.hungry.collection.repositories

import pl.hungry.collection.domain.ConfirmedCollection

trait ConfirmedCollectionRepository[F[_]] {
  def insert(confirmedCollection: ConfirmedCollection): F[Int]
}
