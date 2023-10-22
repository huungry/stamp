package pl.hungry.user.services

import cats.data.EitherT
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.user.domain.{User, UserId, UserView}
import pl.hungry.user.repositories.UserRepository
import pl.hungry.user.services.FindMeService.FindMeError
import pl.hungry.utils.error.DomainError

class FindMeService(userRepository: UserRepository[ConnectionIO], transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, FindMeError, T]

  def find(authContext: AuthContext): IO[Either[FindMeError, UserView]] = {
    val effect = for {
      user <- findUser(authContext.userId)
    } yield UserView.from(user)

    effect.value.transact(transactor)
  }

  private def findUser(userId: UserId): ErrorOr[User] =
    EitherT.fromOptionF(userRepository.find(userId), FindMeError.NotFound())
}

object FindMeService {
  sealed trait FindMeError extends DomainError
  object FindMeError {
    case class NotFound(message: String = "Unexpected error when getting my user - not found") extends FindMeError
  }
}
