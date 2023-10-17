package pl.hungry.reward.domain

import eu.timepit.refined.types.string.NonEmptyString

import java.util.UUID

final case class RewardId(value: UUID) extends AnyVal
object RewardId {
  def generate: RewardId = RewardId(UUID.randomUUID())
}
final case class RewardName(value: NonEmptyString)
