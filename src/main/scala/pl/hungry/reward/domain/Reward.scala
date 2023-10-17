package pl.hungry.reward.domain

import io.scalaland.chimney.dsl._
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.routers.in.CreateRewardRequest

import java.time.Instant

final case class Reward(
  id: RewardId,
  name: RewardName,
  restaurantId: RestaurantId,
  archivedAt: Option[Instant])

object Reward {
  def from(createRewardRequest: CreateRewardRequest, restaurantId: RestaurantId): Reward =
    createRewardRequest
      .into[Reward]
      .withFieldConst(_.id, RewardId.generate)
      .withFieldConst(_.restaurantId, restaurantId)
      .withFieldConst(_.archivedAt, None)
      .transform
}
