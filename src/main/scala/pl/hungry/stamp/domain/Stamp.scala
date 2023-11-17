package pl.hungry.stamp.domain

import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stamp.routers.in.CreateStampRequest
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class Stamp(
  id: StampId,
  restaurantId: RestaurantId,
  userId: UserId,
  createdAt: Instant,
  usedAt: Option[Instant])

object Stamp {
  def from(
    restaurantId: RestaurantId,
    request: CreateStampRequest,
    now: Instant
  ): Stamp =
    Stamp(id = StampId.generate, restaurantId = restaurantId, userId = request.forUserId, createdAt = now, usedAt = None)
}
