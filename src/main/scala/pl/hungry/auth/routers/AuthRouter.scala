package pl.hungry.auth.routers

import cats.effect.IO
import pl.hungry.auth.domain.{AuthContext, JwtToken}
import pl.hungry.auth.routers.AuthRouter._
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.auth.services.AuthService.AuthError
import pl.hungry.auth.services.LoginService.LoginError
import pl.hungry.auth.services.{AuthService, LoginService}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

class AuthRouter(authService: AuthService, loginService: LoginService) {
  import pl.hungry.auth.protocols.AuthCodecs._
  import pl.hungry.user.protocols.UserSchemas._
  import pl.hungry.utils.error.DomainErrorCodecs._

  val bearerEndpoint: BearerEndpoint =
    endpoint
      .securityIn(auth.bearer[JwtToken]())
      .errorOut(statusCode(StatusCode.Forbidden))
      .errorOut(jsonBody[AuthError])
      .serverSecurityLogic(authService.decode)

  private val loginEndpoint: ServerEndpoint[Any, IO] = endpoint.post
    .in(authPath / loginPath)
    .in(jsonBody[LoginRequest])
    .errorOut(
      oneOf[LoginError](
        oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[LoginError.InvalidCredentials])),
        oneOfDefaultVariant(statusCode(StatusCode.Forbidden).and(jsonBody[LoginError]))
      )
    )
    .out(jsonBody[JwtToken])
    .serverLogic(loginService.login)

  val all: List[ServerEndpoint[Any, IO]] = List(loginEndpoint)
}

object AuthRouter {
  private val authPath  = "auth"
  private val loginPath = "login"

  type BearerEndpoint = PartialServerEndpoint[JwtToken, AuthContext, Unit, AuthError, Unit, Any, IO]
}
