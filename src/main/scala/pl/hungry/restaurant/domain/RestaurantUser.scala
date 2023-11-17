package pl.hungry.restaurant.domain

import pl.hungry.user.domain.UserId

import java.time.Instant

final case class RestaurantUser(
  id: RestaurantUserId,
  restaurantId: RestaurantId,
  userId: UserId,
  position: Position,
  createdAt: Instant,
  archivedAt: Option[Instant]) {

  def archived(now: Instant): RestaurantUser =
    copy(archivedAt = Some(now))
}

object RestaurantUser {
  def from(
    restaurant: Restaurant,
    userId: UserId,
    position: Position,
    now: Instant
  ): RestaurantUser =
    RestaurantUser(
      id = RestaurantUserId.generate,
      restaurantId = restaurant.id,
      userId = userId,
      position = position,
      createdAt = now,
      archivedAt = None
    )
}
