package pl.hungry.restaurant.repositories

import cats.data.NonEmptyList
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId}
import pl.hungry.user.domain.UserId

trait RestaurantRepository[F[_]] {
  def countActive(userId: UserId): F[Int]
  def findActive(id: RestaurantId): F[Option[Restaurant]]
  def insert(restaurant: Restaurant): F[Int]
  def list(ids: NonEmptyList[RestaurantId]): F[List[Restaurant]]
}
