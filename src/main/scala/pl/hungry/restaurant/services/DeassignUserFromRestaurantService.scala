package pl.hungry.restaurant.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantUserRepository}
import pl.hungry.restaurant.services.DeassignUserFromRestaurantService.DeassignUserFromRestaurantError
import pl.hungry.user.domain.{UserId, UserView}
import pl.hungry.user.services.UserInternalService
import pl.hungry.utils.error.DomainError

import java.time.Instant

class DeassignUserFromRestaurantService(
  userInternalService: UserInternalService,
  restaurantRepository: RestaurantRepository[ConnectionIO],
  restaurantUserRepository: RestaurantUserRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, DeassignUserFromRestaurantError, T]

  def deassign(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    userIdToDeassign: UserId
  ): IO[Either[DeassignUserFromRestaurantError, Unit]] = {
    val effect = for {
      user           <- findUser(authContext.userId)
      _              <- findRestaurant(restaurantId)
      _              <- ensureIsAuthorized(user.id, restaurantId)
      restaurantUser <- findRestaurantUser(userIdToDeassign, restaurantId)
      now            <- getTime
      _              <- archive(restaurantUser, now)
    } yield ()

    effect.value.transact(transactor)
  }

  private def findUser(userId: UserId): ErrorOr[UserView] =
    EitherT.fromOptionF(userInternalService.find(userId), DeassignUserFromRestaurantError.UserNotFound())

  private def findRestaurant(id: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantRepository.findActive(id), DeassignUserFromRestaurantError.RestaurantNotFound())

  private def ensureIsAuthorized(userId: UserId, restaurantId: RestaurantId): ErrorOr[RestaurantUser] =
    EitherT.fromOptionF(
      restaurantUserRepository.findActiveWithPosition(userId, restaurantId, Position.Manager),
      DeassignUserFromRestaurantError.NotManager()
    )

  private def findRestaurantUser(userId: UserId, restaurantId: RestaurantId): ErrorOr[RestaurantUser] =
    EitherT.fromOptionF(restaurantUserRepository.findActive(userId, restaurantId), DeassignUserFromRestaurantError.RestaurantUserNotFound())

  private def getTime: ErrorOr[Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def archive(restaurantUser: RestaurantUser, now: Instant): ErrorOr[Int] =
    EitherT.right(restaurantUserRepository.archive(restaurantUser.id, now))
}

object DeassignUserFromRestaurantService {
  sealed trait DeassignUserFromRestaurantError extends DomainError
  object DeassignUserFromRestaurantError {
    case class UserNotFound(message: String = "User not found")                               extends DeassignUserFromRestaurantError
    case class RestaurantNotFound(message: String = "Restaurant not found")                   extends DeassignUserFromRestaurantError
    case class RestaurantUserNotFound(message: String = "User is not assigned to restaurant") extends DeassignUserFromRestaurantError
    case class NotManager(message: String = "Only managers can deassign users")               extends DeassignUserFromRestaurantError
  }
}
