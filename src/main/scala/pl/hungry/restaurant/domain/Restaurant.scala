package pl.hungry.restaurant.domain

import io.scalaland.chimney.dsl._
import pl.hungry.restaurant.routers.in.CreateRestaurantRequest
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class Restaurant(
  id: RestaurantId,
  email: RestaurantEmail,
  name: RestaurantName,
  createdBy: UserId,
  createdAt: Instant,
  archivedAt: Option[Instant])

object Restaurant {
  def from(
    createRestaurantRequest: CreateRestaurantRequest,
    userId: UserId,
    now: Instant
  ): Restaurant =
    createRestaurantRequest
      .into[Restaurant]
      .withFieldConst(_.id, RestaurantId.generate)
      .withFieldConst(_.createdBy, userId)
      .withFieldConst(_.createdAt, now)
      .withFieldConst(_.archivedAt, None)
      .transform
}
