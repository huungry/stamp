package pl.hungry.collection.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.collection.domain.CollectionId
import pl.hungry.collection.domain.dto.{ConfirmedCollectionDto, UnconfirmedCollectionDto}
import pl.hungry.collection.routers.CollectionRouter._
import pl.hungry.collection.services.ConfirmCollectionService.ConfirmCollectionError
import pl.hungry.collection.services.CreateCollectionService.CreateCollectionError
import pl.hungry.collection.services.{ConfirmCollectionService, CreateCollectionService}
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.RewardId
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class CollectionRouter(
  bearerEndpoint: BearerEndpoint,
  createCollectionService: CreateCollectionService,
  confirmCollectionService: ConfirmCollectionService) {
  import pl.hungry.collection.protocols.CollectionCodecs._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createCollectionEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurantsPath / path[RestaurantId] / rewardsPath / path[RewardId] / collectionsPath)
      .out(jsonBody[UnconfirmedCollectionDto])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateCollectionError.RewardNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateCollectionError.RestaurantNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateCollectionError.StampConfigNotFound])),
        oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateCollectionError.RewardDoesNotMatchRestaurant])),
        oneOfVariant(statusCode(StatusCode.Conflict).and(jsonBody[CreateCollectionError.NotEnoughStamps])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateCollectionError]))
      )
      .serverLogic(authContext =>
        input => {
          val (restaurantId, rewardId) = input
          createCollectionService.create(authContext, restaurantId, rewardId)
        }
      )

  private val confirmCollectionEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurantsPath / path[RestaurantId] / rewardsPath / path[RewardId] / collectionsPath / path[CollectionId] / confirmPath)
      .out(jsonBody[ConfirmedCollectionDto])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ConfirmCollectionError.UnconfirmedCollectionNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ConfirmCollectionError.RewardNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ConfirmCollectionError.RestaurantNotFound])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[ConfirmCollectionError.RestaurantUserNotFound])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[ConfirmCollectionError]))
      )
      .serverLogic(authContext =>
        input => {
          val (_, _, collectionId) = input
          confirmCollectionService.confirm(authContext, collectionId)
        }
      )

  val all: List[ServerEndpoint[Any, IO]] = List(createCollectionEndpoint, confirmCollectionEndpoint)
}

object CollectionRouter {
  private val restaurantsPath = "restaurants"
  private val rewardsPath     = "rewards"
  private val collectionsPath = "collections"
  private val confirmPath     = "confirm"
}
