package pl.hungry.restaurant.protocols

import pl.hungry.restaurant.domain.{Position, RestaurantEmail, RestaurantName}
import sttp.tapir.Schema

object RestaurantSchemas {
  implicit val restaurantNameSchema: Schema[RestaurantName]   = Schema.string
  implicit val restaurantEmailSchema: Schema[RestaurantEmail] = Schema.string

  implicit val restaurantPositionCodec: Schema[Position] = Schema.derivedEnumeration[Position].defaultStringBased
}
