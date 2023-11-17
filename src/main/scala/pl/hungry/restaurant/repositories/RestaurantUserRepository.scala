package pl.hungry.restaurant.repositories

import pl.hungry.restaurant.domain.{Position, RestaurantId, RestaurantUser, RestaurantUserId}
import pl.hungry.user.domain.UserId

import java.time.Instant

trait RestaurantUserRepository[F[_]] {
  def findActive(userId: UserId, restaurantId: RestaurantId): F[Option[RestaurantUser]]
  def findActiveWithPosition(
    userId: UserId,
    restaurantId: RestaurantId,
    position: Position
  ): F[Option[RestaurantUser]]
  def listActive(userId: UserId): F[List[RestaurantUser]]
  def insert(restaurantUser: RestaurantUser): F[Int]
  def archive(restaurantUserId: RestaurantUserId, now: Instant): F[Int]
}
