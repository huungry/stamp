package pl.hungry.auth.services

import cats.data.EitherT
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.{AuthUser, JwtToken}
import pl.hungry.auth.repositories.AuthRepository
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.auth.services.LoginService.LoginError
import pl.hungry.user.domain._
import pl.hungry.utils.error.DomainError

class LoginService(
  authService: AuthService,
  authRepository: AuthRepository[ConnectionIO],
  passwordService: PasswordService,
  transactor: Transactor[IO]) {
  def login(request: LoginRequest): IO[Either[LoginError, JwtToken]] = {
    val effect = for {
      user  <- ensureUserExists(request.email)
      _     <- validateCredentials(request.password, user.passwordHash)
      token <- generateToken(user.id)
    } yield token

    effect.value.transact(transactor)
  }

  private def ensureUserExists(email: UserEmail): EitherT[ConnectionIO, LoginError, AuthUser] =
    EitherT.fromOptionF(authRepository.find(email), LoginError.InvalidCredentials())

  private def validateCredentials(candidate: PasswordPlain, hash: PasswordHash): EitherT[ConnectionIO, LoginError, Unit] =
    EitherT.cond[ConnectionIO](passwordService.isValid(candidate, hash), (), LoginError.InvalidCredentials())

  private def generateToken(userId: UserId): EitherT[ConnectionIO, LoginError, JwtToken] =
    EitherT.fromOptionF[ConnectionIO, LoginError, JwtToken](authService.encode(userId), LoginError.InvalidCredentials())
}

object LoginService {
  sealed trait LoginError extends DomainError
  object LoginError {
    case class InvalidCredentials(message: String = "Invalid credentials") extends LoginError
  }
}
