package pl.hungry.collection.repositories

import pl.hungry.collection.domain.{CollectionId, UnconfirmedCollection}

trait UnconfirmedCollectionRepository[F[_]] {
  def delete(id: CollectionId): F[Int]
  def find(id: CollectionId): F[Option[UnconfirmedCollection]]
  def insert(unconfirmedCollection: UnconfirmedCollection): F[Int]
}
