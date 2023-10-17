package pl.hungry.restaurant.services

import doobie._
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantUserRepository}
import pl.hungry.user.domain.UserId

class RestaurantInternalService(
  restaurantRepository: RestaurantRepository[ConnectionIO],
  restaurantUserRepository: RestaurantUserRepository[ConnectionIO]) {
  def findActive(restaurantId: RestaurantId): ConnectionIO[Option[Restaurant]] = restaurantRepository.findActive(restaurantId)

  def findRestaurantUser(userId: UserId, restaurantId: RestaurantId): ConnectionIO[Option[RestaurantUser]] =
    restaurantUserRepository.findActive(userId, restaurantId)
}
