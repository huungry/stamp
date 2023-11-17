package pl.hungry.stampconfig

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.services.RewardInternalService
import pl.hungry.stampconfig.repositories.{StampConfigRepository, StampConfigRepositoryDoobie}
import pl.hungry.stampconfig.routers.StampConfigRouter
import pl.hungry.stampconfig.services.{CreateStampConfigService, FindStampConfigService, StampConfigInternalService}

final case class StampConfigModule(routes: StampConfigRouter, stampConfigInternalService: StampConfigInternalService)

object StampConfigModule {
  def make(
    transactor: Transactor[IO],
    bearerEndpoint: BearerEndpoint,
    restaurantInternalService: RestaurantInternalService,
    rewardInternalService: RewardInternalService
  ): StampConfigModule = {

    val stampConfigRepository: StampConfigRepository[ConnectionIO] = new StampConfigRepositoryDoobie
    val createStampConfigService: CreateStampConfigService =
      new CreateStampConfigService(restaurantInternalService, rewardInternalService, stampConfigRepository, transactor)
    val findStampConfigService: FindStampConfigService = new FindStampConfigService(stampConfigRepository, transactor)
    val stampConfigInternalService: StampConfigInternalService =
      new StampConfigInternalService(stampConfigRepository)
    val stampConfigRouter: StampConfigRouter = new StampConfigRouter(bearerEndpoint, createStampConfigService, findStampConfigService)

    StampConfigModule(stampConfigRouter, stampConfigInternalService)
  }
}
