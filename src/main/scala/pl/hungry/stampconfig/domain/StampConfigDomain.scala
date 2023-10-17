package pl.hungry.stampconfig.domain

import java.util.UUID

final case class StampConfigId(value: UUID) extends AnyVal
object StampConfigId {
  def generate: StampConfigId = StampConfigId(UUID.randomUUID())
}
