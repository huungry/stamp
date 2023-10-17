package pl.hungry.collection

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.collection.repositories._
import pl.hungry.collection.routers.CollectionRouter
import pl.hungry.collection.services.{ConfirmCollectionService, CreateCollectionService}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.services.RewardInternalService
import pl.hungry.stamp.services.StampInternalService
import pl.hungry.stampconfig.services.StampConfigInternalService

final case class CollectionModule(routes: CollectionRouter)

object CollectionModule {
  def make(
    transactor: Transactor[IO],
    bearerEndpoint: BearerEndpoint,
    restaurantInternalService: RestaurantInternalService,
    rewardInternalService: RewardInternalService,
    stampConfigInternalService: StampConfigInternalService,
    stampInternalService: StampInternalService
  ): CollectionModule = {
    val unconfirmedCollectionRepository: UnconfirmedCollectionRepository[ConnectionIO] =
      new UnconfirmedCollectionRepositoryDoobie
    val confirmedCollectionRepository: ConfirmedCollectionRepository[ConnectionIO] =
      new ConfirmedCollectionRepositoryDoobie

    val createCollectionService: CreateCollectionService = new CreateCollectionService(
      rewardInternalService,
      restaurantInternalService,
      stampConfigInternalService,
      stampInternalService,
      unconfirmedCollectionRepository,
      transactor
    )
    val confirmCollectionService: ConfirmCollectionService = new ConfirmCollectionService(
      confirmedCollectionRepository,
      rewardInternalService,
      restaurantInternalService,
      unconfirmedCollectionRepository,
      transactor
    )

    val collectionRouter = new CollectionRouter(bearerEndpoint, createCollectionService, confirmCollectionService)

    CollectionModule(collectionRouter)
  }
}
