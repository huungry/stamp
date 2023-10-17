package pl.hungry.restaurant.domain

import io.scalaland.chimney.dsl._
import pl.hungry.restaurant.routers.in.CreateRestaurantRequest

import java.time.Instant

final case class Restaurant(
  id: RestaurantId,
  email: RestaurantEmail,
  name: RestaurantName,
  createdAt: Instant,
  archivedAt: Option[Instant])

object Restaurant {
  def from(createRestaurantRequest: CreateRestaurantRequest, now: Instant): Restaurant =
    createRestaurantRequest
      .into[Restaurant]
      .withFieldConst(_.id, RestaurantId.generate)
      .withFieldConst(_.createdAt, now)
      .withFieldConst(_.archivedAt, None)
      .transform
}
