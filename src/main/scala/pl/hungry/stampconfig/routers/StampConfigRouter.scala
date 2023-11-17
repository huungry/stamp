package pl.hungry.stampconfig.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.routers.StampConfigRouter._
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest
import pl.hungry.stampconfig.services.CreateStampConfigService.CreateStampConfigError
import pl.hungry.stampconfig.services.FindStampConfigService.FindStampConfigError
import pl.hungry.stampconfig.services.{CreateStampConfigService, FindStampConfigService}
import sttp.model._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class StampConfigRouter(
  bearerEndpoint: BearerEndpoint,
  createStampConfigService: CreateStampConfigService,
  findStampConfigService: FindStampConfigService) {
  import pl.hungry.stampconfig.protocols.StampConfigCodecs._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createStampConfigEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurantsPath / path[RestaurantId] / stampsPath / configPath)
      .in(jsonBody[CreateStampConfigRequest])
      .out(jsonBody[StampConfig])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateStampConfigError.RestaurantNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateStampConfigError.RestaurantUserNotFound])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[CreateStampConfigError.NotManager])),
        oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateStampConfigError.InvalidRewards])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateStampConfigError]))
      )
      .serverLogic(userId =>
        input => {
          val (restaurantId, request) = input
          createStampConfigService.create(userId, restaurantId, request)
        }
      )

  private val findStampConfigEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.get
      .in(restaurantsPath / path[RestaurantId] / stampsPath / configPath)
      .out(jsonBody[StampConfig])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[FindStampConfigError.StampConfigNotFound])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[FindStampConfigError]))
      )
      .serverLogic(_ => restaurantId => findStampConfigService.find(restaurantId)) // todo auth

  val all: List[ServerEndpoint[Any, IO]] = List(createStampConfigEndpoint, findStampConfigEndpoint)
}

object StampConfigRouter {
  private val restaurantsPath = "restaurants"
  private val stampsPath      = "stamps"
  private val configPath      = "configs"
}
