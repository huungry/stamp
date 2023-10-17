package pl.hungry.restaurant.routers.in

import pl.hungry.restaurant.domain.{RestaurantEmail, RestaurantName}

final case class CreateRestaurantRequest(email: RestaurantEmail, name: RestaurantName)
