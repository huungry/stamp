package pl.hungry.reward.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.Reward
import pl.hungry.reward.routers.RewardRouter._
import pl.hungry.reward.routers.in.CreateRewardRequest
import pl.hungry.reward.services.CreateRewardService.CreateRewardError
import pl.hungry.reward.services.ListRewardService.ListRewardError
import pl.hungry.reward.services.{CreateRewardService, ListRewardService}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class RewardRouter(
  bearerEndpoint: BearerEndpoint,
  createRewardService: CreateRewardService,
  listRewardService: ListRewardService) {
  import pl.hungry.reward.protocols.RewardCodecs._
  import pl.hungry.reward.protocols.RewardSchemas._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createRewardEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurantsPath / path[RestaurantId] / rewardsPath)
      .in(jsonBody[CreateRewardRequest])
      .out(jsonBody[Reward])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateRewardError.RestaurantUserNotFound])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[CreateRewardError.NotManager])),
        oneOfVariant(statusCode(StatusCode.Conflict).and(jsonBody[CreateRewardError.NameAlreadyExists])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateRewardError]))
      )
      .serverLogic(userId =>
        input => {
          val (restaurantId, request) = input
          createRewardService.create(userId, restaurantId, request)
        }
      )

  private val listRewardEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.get
      .in(restaurantsPath / path[RestaurantId] / rewardsPath)
      .out(jsonBody[List[Reward]])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ListRewardError.RestaurantNotFound])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[ListRewardError]))
      )
      .serverLogic(_ => restaurantId => listRewardService.listActive(restaurantId))

  val all: List[ServerEndpoint[Any, IO]] = List(createRewardEndpoint, listRewardEndpoint)
}

object RewardRouter {
  private val restaurantsPath = "restaurants"
  private val rewardsPath     = "rewards"
}
