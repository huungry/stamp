package pl.hungry.restaurant.services

import cats.data.NonEmptyList
import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantUserRepository}
import pl.hungry.user.domain.UserId

class ListRestaurantService(
  restaurantRepository: RestaurantRepository[ConnectionIO],
  restaurantUserRepository: RestaurantUserRepository[ConnectionIO],
  transactor: Transactor[IO]) {
  def listRelated(authContext: AuthContext): IO[List[Restaurant]] = {
    val effect = for {
      restaurantUsers <- listRestaurantUsers(authContext.userId)
      restaurantsIds = restaurantUsers.map(_.restaurantId)
      restaurants <- listRestaurants(restaurantsIds)
    } yield restaurants

    effect.transact(transactor)
  }

  private def listRestaurantUsers(userId: UserId): ConnectionIO[List[RestaurantUser]] =
    restaurantUserRepository.listActive(userId)

  private def listRestaurants(restaurantsIds: List[RestaurantId]): ConnectionIO[List[Restaurant]] =
    NonEmptyList
      .fromList(restaurantsIds)
      .fold(WeakAsyncConnectionIO.pure(List.empty[Restaurant]))(ids => restaurantRepository.list(ids))
}
