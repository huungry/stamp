package pl.hungry.stamp.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stamp.domain.{Stamp, StampView}
import pl.hungry.stamp.routers.StampRouter._
import pl.hungry.stamp.routers.in.CreateStampRequest
import pl.hungry.stamp.services.CreateStampService.CreateStampError
import pl.hungry.stamp.services.{CreateStampService, ListStampViewService}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class StampRouter(
  bearerEndpoint: BearerEndpoint,
  createStampService: CreateStampService,
  listStampService: ListStampViewService) {
  import pl.hungry.stamp.protocols.StampCodecs._
  import pl.hungry.restaurant.protocols.RestaurantSchemas._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createStampEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurantsPath / path[RestaurantId] / stampsPath)
      .in(jsonBody[CreateStampRequest])
      .out(jsonBody[Stamp])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateStampError.ClientNotFound])),
        oneOfVariant(statusCode(StatusCode.Conflict).and(jsonBody[CreateStampError.UserRequestedThemself])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[CreateStampError.RestaurantUserNotFound])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateStampError]))
      )
      .serverLogic { userId => input =>
        val (restaurantId, request) = input
        createStampService.create(userId, restaurantId, request)
      }

  private val listStampViewEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.get
      .in(restaurantsPath / stampsPath)
      .out(jsonBody[List[StampView]])
      .serverLogicSuccess(userId => _ => listStampService.listView(userId))

  val all: List[ServerEndpoint[Any, IO]] = List(createStampEndpoint, listStampViewEndpoint)
}

object StampRouter {
  private val restaurantsPath = "restaurants"
  private val stampsPath      = "stamps"
}
