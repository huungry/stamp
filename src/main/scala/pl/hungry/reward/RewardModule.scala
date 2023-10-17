package pl.hungry.reward

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.repositories.{RewardRepository, RewardRepositoryDoobie}
import pl.hungry.reward.routers.RewardRouter
import pl.hungry.reward.services.{CreateRewardService, ListRewardService, RewardInternalService}

final case class RewardModule(routes: RewardRouter, rewardInternalService: RewardInternalService)

object RewardModule {
  def make(
    transactor: Transactor[IO],
    bearerEndpoint: BearerEndpoint,
    restaurantInternalService: RestaurantInternalService
  ): RewardModule = {
    val rewardRepository: RewardRepository[ConnectionIO] = new RewardRepositoryDoobie
    val createRewardService: CreateRewardService         = new CreateRewardService(rewardRepository, restaurantInternalService, transactor)
    val listRewardService: ListRewardService             = new ListRewardService(rewardRepository, restaurantInternalService, transactor)
    val rewardInternalService: RewardInternalService     = new RewardInternalService(rewardRepository)
    val rewardRouter: RewardRouter                       = new RewardRouter(bearerEndpoint, createRewardService, listRewardService)

    RewardModule(rewardRouter, rewardInternalService)
  }
}
