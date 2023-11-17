package pl.hungry.collection.domain

import java.util.UUID

final case class CollectionId(value: UUID) extends AnyVal
object CollectionId {
  def generate: CollectionId = CollectionId(UUID.randomUUID())
}
