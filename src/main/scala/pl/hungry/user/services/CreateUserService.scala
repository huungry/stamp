package pl.hungry.user.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.services.PasswordService
import pl.hungry.user.domain.{User, UserEmail, UserView}
import pl.hungry.user.repositories.UserRepository
import pl.hungry.user.routers.in.CreateUserRequest
import pl.hungry.user.services.CreateUserService.CreateUserError
import pl.hungry.utils.error.DomainError

import java.time.Instant

class CreateUserService(
  userRepository: UserRepository[ConnectionIO],
  passwordService: PasswordService,
  transactor: Transactor[IO]) {
  def create(request: CreateUserRequest): IO[Either[CreateUserError, UserView]] = {
    val effect = for {
      _   <- ensureEmailNotUsed(request.email)
      now <- getTime
      user = prepareUser(request, now)
      _ <- insert(user)
    } yield UserView.from(user)

    effect.value.transact(transactor)
  }

  private def ensureEmailNotUsed(email: UserEmail): EitherT[ConnectionIO, CreateUserError.EmailAlreadyUsed, Unit] =
    for {
      userOpt <- EitherT.liftF(userRepository.find(email))
      _       <- EitherT.fromEither[ConnectionIO](userOpt.map(_ => CreateUserError.EmailAlreadyUsed().asLeft).getOrElse(().asRight))
    } yield ()

  private def getTime: EitherT[ConnectionIO, CreateUserError, Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def prepareUser(request: CreateUserRequest, now: Instant): User = {
    val passwordHash = passwordService.hash(request.password)
    User.from(request, passwordHash, now)
  }

  private def insert(user: User): EitherT[ConnectionIO, CreateUserError, Int] =
    EitherT.liftF(userRepository.insert(user))
}

object CreateUserService {
  sealed trait CreateUserError extends DomainError
  object CreateUserError {
    case class EmailAlreadyUsed(message: String = "Email already used") extends CreateUserError
  }
}
