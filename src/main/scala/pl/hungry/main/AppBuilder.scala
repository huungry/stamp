package pl.hungry.main

import cats.effect.IO
import cats.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.AuthModule
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.collection.CollectionModule
import pl.hungry.restaurant.RestaurantModule
import pl.hungry.reward.RewardModule
import pl.hungry.stamp.StampModule
import pl.hungry.stampconfig.StampConfigModule
import pl.hungry.user.UserModule
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object AppBuilder {

  def buildModules(transactor: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
    val authModule                     = AuthModule.make(transactor)
    val bearerEndpoint: BearerEndpoint = authModule.routes.bearerEndpoint

    val userModule       = UserModule.make(transactor, authModule.passwordService, bearerEndpoint)
    val restaurantModule = RestaurantModule.make(transactor, bearerEndpoint, userModule.userInternalService)
    val rewardModule     = RewardModule.make(transactor, bearerEndpoint, restaurantModule.restaurantInternalService)
    val stampModule      = StampModule.make(transactor, bearerEndpoint, userModule.userInternalService, restaurantModule.restaurantInternalService)
    val stampConfigModule =
      StampConfigModule.make(transactor, bearerEndpoint, restaurantModule.restaurantInternalService, rewardModule.rewardInternalService)

    val collectionModule = CollectionModule.make(
      transactor,
      bearerEndpoint,
      restaurantModule.restaurantInternalService,
      rewardModule.rewardInternalService,
      stampConfigModule.stampConfigInternalService,
      stampModule.stampInternalService
    )

    val apiEndpoints: List[ServerEndpoint[Any, IO]] =
      authModule.routes.all |+|
        collectionModule.routes.all |+|
        restaurantModule.routes.all |+|
        rewardModule.routes.all |+|
        stampModule.routes.all |+|
        stampConfigModule.routes.all |+|
        userModule.routes.all

    val docEndpoints: List[ServerEndpoint[Any, IO]] = SwaggerInterpreter().fromServerEndpoints[IO](apiEndpoints, "stamp", "1.0.0")

    apiEndpoints ++ docEndpoints
  }
}
