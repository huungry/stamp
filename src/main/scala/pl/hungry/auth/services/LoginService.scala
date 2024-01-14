package pl.hungry.auth.services

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Clock
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import pl.hungry.auth.domain._
import pl.hungry.auth.repositories.{AuthRepository, UserRefreshTokenRepository}
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.auth.services.LoginService.LoginError
import pl.hungry.user.domain._
import pl.hungry.utils.error.DomainError

class LoginService(
  authService: AuthService,
  authRepository: AuthRepository[ConnectionIO],
  userRefreshTokenRepository: UserRefreshTokenRepository[ConnectionIO],
  passwordService: PasswordService,
  transactor: Transactor[IO]
)(implicit loggerFactory: LoggerFactory[IO]) {
  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLogger

  private type ErrorOr[T] = EitherT[IO, LoginError, T]

  def login(request: LoginRequest, userAgentOpt: Option[UserAgent]): IO[Either[LoginError, LoginResponse]] = {
    val effect = for {
      user            <- ensureUserExists(request.email)
      _               <- validateCredentials(request.password, user.passwordHash)
      token           <- generateToken(user)
      refreshTokenOpt <- tryInsertRefreshToken(user.id, userAgentOpt)
    } yield LoginResponse(token, refreshTokenOpt)

    effect.value
  }

  private def ensureUserExists(email: UserEmail): ErrorOr[AuthUser] =
    EitherT.fromOptionF(authRepository.find(email).transact(transactor), LoginError.InvalidCredentials())

  private def validateCredentials(candidate: PasswordPlain, hash: PasswordHash): ErrorOr[Unit] =
    EitherT.cond[IO](passwordService.isValid(candidate, hash), (), LoginError.InvalidCredentials())

  private def generateToken(user: AuthUser): ErrorOr[JwtToken] =
    EitherT.right(authService.encode(user))

  private def tryInsertRefreshToken(userId: UserId, userAgentOpt: Option[UserAgent]): ErrorOr[Option[RefreshToken]] =
    userAgentOpt match {
      case None => EitherT.right(logger.warn("User-Agent header is empty - cannot insert refresh token").as(Option.empty[RefreshToken]))
      case Some(userAgent) =>
        EitherT.right {
          Clock[IO].realTimeInstant.flatMap { now =>
            val refreshToken     = RefreshToken.generate
            val userRefreshToken = UserRefreshToken.from(userId, RefreshToken.generate, userAgent, now)
            userRefreshTokenRepository.upsert(userRefreshToken).transact(transactor).map(_ => Some(refreshToken))
          }
        }
    }
}

object LoginService {
  sealed trait LoginError extends DomainError
  object LoginError {
    case class InvalidCredentials(message: String = "Invalid credentials") extends LoginError
  }
}
