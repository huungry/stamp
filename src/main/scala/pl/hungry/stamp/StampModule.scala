package pl.hungry.stamp

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.stamp.repositories.{StampRepository, StampRepositoryDoobie}
import pl.hungry.stamp.routers.StampRouter
import pl.hungry.stamp.services.{CreateStampService, ListStampViewService, StampInternalService}
import pl.hungry.user.services.UserInternalService

final case class StampModule(routes: StampRouter, stampInternalService: StampInternalService)

object StampModule {
  def make(
    transactor: Transactor[IO],
    bearerEndpoint: BearerEndpoint,
    userInternalService: UserInternalService,
    restaurantInternalService: RestaurantInternalService
  ): StampModule = {

    val stampRepository: StampRepository[ConnectionIO] = new StampRepositoryDoobie
    val createStampService: CreateStampService = new CreateStampService(stampRepository, restaurantInternalService, userInternalService, transactor)
    val listStampService: ListStampViewService     = new ListStampViewService(stampRepository, transactor)
    val stampInternalService: StampInternalService = new StampInternalService(stampRepository)
    val stampRouter: StampRouter                   = new StampRouter(bearerEndpoint, createStampService, listStampService)

    StampModule(stampRouter, stampInternalService)
  }
}
