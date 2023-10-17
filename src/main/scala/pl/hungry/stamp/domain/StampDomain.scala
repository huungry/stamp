package pl.hungry.stamp.domain

import java.util.UUID

final case class StampId(value: UUID) extends AnyVal
object StampId {
  def generate: StampId = StampId(UUID.randomUUID())
}
