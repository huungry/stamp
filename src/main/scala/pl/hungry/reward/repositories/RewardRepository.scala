package pl.hungry.reward.repositories

import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.{Reward, RewardId, RewardName}

trait RewardRepository[F[_]] {
  def listActive(restaurantId: RestaurantId): F[List[Reward]]
  def findActive(id: RewardId): F[Option[Reward]]
  def findActive(name: RewardName, restaurantId: RestaurantId): F[Option[Reward]]
  def insert(reward: Reward): F[Int]
}
