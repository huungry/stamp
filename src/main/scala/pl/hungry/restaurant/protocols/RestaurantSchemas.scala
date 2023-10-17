package pl.hungry.restaurant.protocols

import pl.hungry.restaurant.domain.{RestaurantEmail, RestaurantName}
import sttp.tapir.Schema

object RestaurantSchemas {
  implicit val restaurantNameSchema: Schema[RestaurantName]   = Schema.string
  implicit val restaurantEmailSchema: Schema[RestaurantEmail] = Schema.string
}
