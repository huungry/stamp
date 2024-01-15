package pl.hungry.restaurant

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantRepositoryDoobie, RestaurantUserRepository, RestaurantUserRepositoryDoobie}
import pl.hungry.restaurant.routers.RestaurantRouter
import pl.hungry.restaurant.services._
import pl.hungry.user.services.UserInternalService

final case class RestaurantModule(routes: RestaurantRouter, restaurantInternalService: RestaurantInternalService)

object RestaurantModule {
  def make(
    transactor: Transactor[IO],
    bearerEndpoint: BearerEndpoint,
    userInternalService: UserInternalService
  ): RestaurantModule = {
    val restaurantUserRepository: RestaurantUserRepository[ConnectionIO] = new RestaurantUserRepositoryDoobie
    val restaurantRepository: RestaurantRepository[ConnectionIO]         = new RestaurantRepositoryDoobie
    val createRestaurantService: CreateRestaurantService =
      new CreateRestaurantService(userInternalService, restaurantRepository, restaurantUserRepository, transactor)
    val listRestaurantService: ListRestaurantService         = new ListRestaurantService(restaurantRepository, restaurantUserRepository, transactor)
    val restaurantInternalService: RestaurantInternalService = new RestaurantInternalService(restaurantRepository, restaurantUserRepository)
    val assignUserToRestaurantService: AssignUserToRestaurantService =
      new AssignUserToRestaurantService(userInternalService, restaurantRepository, restaurantUserRepository, transactor)
    val deassignUserFromRestaurantService =
      new DeassignUserFromRestaurantService(userInternalService, restaurantRepository, restaurantUserRepository, transactor)
    val restaurantRouter = new RestaurantRouter(
      bearerEndpoint,
      assignUserToRestaurantService,
      deassignUserFromRestaurantService,
      createRestaurantService,
      listRestaurantService
    )

    RestaurantModule(restaurantRouter, restaurantInternalService)
  }
}
