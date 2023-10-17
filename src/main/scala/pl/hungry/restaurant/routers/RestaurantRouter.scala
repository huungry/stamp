package pl.hungry.restaurant.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.routers.RestaurantRouter._
import pl.hungry.restaurant.routers.in.{AssignUserToRestaurantRequest, CreateRestaurantRequest}
import pl.hungry.restaurant.services.AssignUserToRestaurantService.AssignUserToRestaurantError
import pl.hungry.restaurant.services.CreateRestaurantService.CreateRestaurantError
import pl.hungry.restaurant.services.DeassignUserFromRestaurantService.DeassignUserFromRestaurantError
import pl.hungry.restaurant.services.{
  AssignUserToRestaurantService,
  CreateRestaurantService,
  DeassignUserFromRestaurantService,
  ListRestaurantService
}
import pl.hungry.user.domain.UserId
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class RestaurantRouter(
  bearerEndpoint: BearerEndpoint,
  assignUserToRestaurantService: AssignUserToRestaurantService,
  deassignUserFromRestaurantService: DeassignUserFromRestaurantService,
  createRestaurantService: CreateRestaurantService,
  listRestaurantService: ListRestaurantService) {
  import pl.hungry.restaurant.protocols.RestaurantCodecs._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createRestaurantEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurants)
      .in(jsonBody[CreateRestaurantRequest])
      .out(jsonBody[Restaurant])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[CreateRestaurantError.UserNotFound])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[CreateRestaurantError.NotPro])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[CreateRestaurantError]))
      )
      .serverLogic(auth => request => createRestaurantService.create(auth, request))

  private val assignUserToRestaurantEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.post
      .in(restaurants / path[RestaurantId] / users)
      .in(jsonBody[AssignUserToRestaurantRequest])
      .out(jsonBody[RestaurantUser])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[AssignUserToRestaurantError.UserNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[AssignUserToRestaurantError.RestaurantNotFound])),
        oneOfVariant(statusCode(StatusCode.Conflict).and(jsonBody[AssignUserToRestaurantError.AlreadyAssigned])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[AssignUserToRestaurantError.NotManager])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[AssignUserToRestaurantError]))
      )
      .serverLogic { auth => input =>
        val (restaurantId, request) = input
        assignUserToRestaurantService.assign(auth, restaurantId, request)
      }

  private val deassignUserToRestaurantEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.delete
      .in(restaurants / path[RestaurantId] / users / path[UserId])
      .out(jsonBody[Unit])
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[DeassignUserFromRestaurantError.UserNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[DeassignUserFromRestaurantError.RestaurantNotFound])),
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[DeassignUserFromRestaurantError.RestaurantUserNotFound])),
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[DeassignUserFromRestaurantError.NotManager])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[DeassignUserFromRestaurantError]))
      )
      .serverLogic { auth => input =>
        val (restaurantId, userId) = input
        deassignUserFromRestaurantService.deassign(auth, restaurantId, userId)
      }

  private val listRestaurantEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint.get
      .in(restaurants)
      .out(jsonBody[List[Restaurant]])
      .serverLogicSuccess(authContext => _ => listRestaurantService.listRelated(authContext))

  val all: List[ServerEndpoint[Any, IO]] =
    List(createRestaurantEndpoint, assignUserToRestaurantEndpoint, deassignUserToRestaurantEndpoint, listRestaurantEndpoint)
}

object RestaurantRouter {
  private val restaurants = "restaurants"
  private val users       = "users"
}
