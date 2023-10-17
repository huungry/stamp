package pl.hungry.reward.services

import doobie.ConnectionIO
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.{Reward, RewardId}
import pl.hungry.reward.repositories.RewardRepository

class RewardInternalService(rewardRepository: RewardRepository[ConnectionIO]) {
  def findActive(rewardId: RewardId): ConnectionIO[Option[Reward]]       = rewardRepository.findActive(rewardId)
  def listActive(restaurantId: RestaurantId): ConnectionIO[List[Reward]] = rewardRepository.listActive(restaurantId)
}
