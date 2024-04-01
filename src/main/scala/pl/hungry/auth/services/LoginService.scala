package pl.hungry.auth.services

import cats.data.EitherT
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain._
import pl.hungry.auth.repositories.AuthRepository
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.auth.services.LoginService.LoginError
import pl.hungry.user.domain._
import pl.hungry.utils.error.DomainError

class LoginService(
  authService: AuthService,
  authRepository: AuthRepository[ConnectionIO],
  passwordService: PasswordService,
  refreshTokenService: RefreshTokenService,
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[IO, LoginError, T]

  def login(request: LoginRequest, userAgentOpt: Option[UserAgent]): IO[Either[LoginError, LoginResponse]] = {
    val effect = for {
      user            <- ensureUserExists(request.email)
      _               <- validateCredentials(request.password, user.passwordHash)
      token           <- generateToken(user)
      refreshTokenOpt <- insertRefreshToken(user.id, userAgentOpt)
    } yield LoginResponse(token, refreshTokenOpt)

    effect.value
  }

  private def ensureUserExists(email: UserEmail): ErrorOr[AuthUser] =
    EitherT.fromOptionF(authRepository.find(email).transact(transactor), LoginError.InvalidCredentials())

  private def validateCredentials(candidate: PasswordPlain, hash: PasswordHash): ErrorOr[Unit] =
    EitherT.cond[IO](passwordService.isValid(candidate, hash), (), LoginError.InvalidCredentials())

  private def generateToken(user: AuthUser): ErrorOr[JwtToken] =
    EitherT.right(authService.encode(user))

  private def insertRefreshToken(userId: UserId, userAgentOpt: Option[UserAgent]): ErrorOr[Option[RefreshToken]] =
    EitherT.right(refreshTokenService.upsert(userId, userAgentOpt))
}

object LoginService {
  sealed trait LoginError extends DomainError
  object LoginError {
    case class InvalidCredentials(message: String = "Invalid credentials") extends LoginError
  }
}
