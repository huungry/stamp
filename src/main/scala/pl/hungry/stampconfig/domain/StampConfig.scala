package pl.hungry.stampconfig.domain

import eu.timepit.refined.types.numeric.PosInt
import io.scalaland.chimney.dsl._
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.RewardId
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest

import java.time.Instant

final case class StampConfig(
  id: StampConfigId,
  restaurantId: RestaurantId,
  stampsToReward: PosInt,
  rewards: List[RewardId],
  createdAt: Instant,
  archivedAt: Option[Instant])

object StampConfig {
  def from(
    createStampConfigRequest: CreateStampConfigRequest,
    restaurantId: RestaurantId,
    now: Instant
  ): StampConfig =
    createStampConfigRequest
      .into[StampConfig]
      .withFieldConst(_.id, StampConfigId.generate)
      .withFieldConst(_.restaurantId, restaurantId)
      .withFieldConst(_.createdAt, now)
      .withFieldConst(_.archivedAt, None)
      .transform
}
