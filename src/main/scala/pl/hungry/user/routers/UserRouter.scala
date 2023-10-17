package pl.hungry.user.routers

import cats.effect.IO
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.user.domain.UserView
import pl.hungry.user.routers.UserRouter._
import pl.hungry.user.routers.in.CreateUserRequest
import pl.hungry.user.services.CreateUserService.CreateUserError
import pl.hungry.user.services.FindMeService.FindMeError
import pl.hungry.user.services.{CreateUserService, FindMeService}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint

class UserRouter(
  bearerEndpoint: BearerEndpoint,
  createUserService: CreateUserService,
  findMeService: FindMeService) {
  import pl.hungry.user.protocols.UserCodecs._
  import pl.hungry.utils.error.DomainErrorCodecs._

  private val createUserEndpoint: ServerEndpoint[Any, IO] = endpoint.post
    .in(accountsPath / usersPath)
    .in(jsonBody[CreateUserRequest])
    .errorOut(jsonBody[CreateUserError])
    .out(jsonBody[UserView])
    .serverLogic(createUserService.create)

  private val findMeEndpoint: ServerEndpoint[Any, IO] =
    bearerEndpoint
      .in(accountsPath / usersPath / mePath)
      .errorOutVariants(
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[FindMeError.NotFound])),
        oneOfDefaultVariant(statusCode(StatusCode.BadRequest).and(jsonBody[FindMeError]))
      )
      .out(jsonBody[UserView])
      .serverLogic(auth => _ => findMeService.find(auth))

  val all: List[ServerEndpoint[Any, IO]] = List(createUserEndpoint, findMeEndpoint)
}

object UserRouter {
  private val accountsPath = "accounts"
  private val usersPath    = "users"
  private val mePath       = "me"
}
