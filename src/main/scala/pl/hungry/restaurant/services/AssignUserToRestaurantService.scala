package pl.hungry.restaurant.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantUserRepository}
import pl.hungry.restaurant.routers.in.AssignUserToRestaurantRequest
import pl.hungry.restaurant.services.AssignUserToRestaurantService.AssignUserToRestaurantError
import pl.hungry.user.domain.{UserId, UserView}
import pl.hungry.user.services.UserInternalService
import pl.hungry.utils.error.DomainError

import java.time.Instant

class AssignUserToRestaurantService(
  userInternalService: UserInternalService,
  restaurantRepository: RestaurantRepository[ConnectionIO],
  restaurantUserRepository: RestaurantUserRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, AssignUserToRestaurantError, T]

  def assign(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    request: AssignUserToRestaurantRequest
  ): IO[Either[AssignUserToRestaurantError, RestaurantUser]] = {
    val effect = for {
      authUser   <- findUser(authContext.userId)
      _          <- findUser(request.userId)
      restaurant <- findRestaurant(restaurantId)
      _          <- ensureIsAuthorized(authUser.id, restaurantId)
      _          <- ensureNotAlreadyAssigned(request.userId, restaurantId)
      now        <- getTime

      restaurantUser = prepareRestaurantUser(restaurant, request, now)

      _ <- insertRestaurantUser(restaurantUser)
    } yield restaurantUser

    effect.value.transact(transactor)
  }

  private def findUser(userId: UserId): ErrorOr[UserView] =
    EitherT.fromOptionF(userInternalService.find(userId), AssignUserToRestaurantError.UserNotFound())

  private def findRestaurant(id: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantRepository.findActive(id), AssignUserToRestaurantError.RestaurantNotFound())

  private def ensureIsAuthorized(userId: UserId, restaurantId: RestaurantId): ErrorOr[RestaurantUser] =
    EitherT.fromOptionF(
      restaurantUserRepository.findActiveWithPosition(userId, restaurantId, Position.Manager),
      AssignUserToRestaurantError.NotManager()
    )

  private def ensureNotAlreadyAssigned(userId: UserId, restaurantId: RestaurantId): ErrorOr[Unit] =
    EitherT {
      restaurantUserRepository.findActive(userId, restaurantId).map {
        case Some(_) => AssignUserToRestaurantError.AlreadyAssigned().asLeft
        case None    => ().asRight
      }
    }

  private def getTime: ErrorOr[Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def prepareRestaurantUser(
    restaurant: Restaurant,
    request: AssignUserToRestaurantRequest,
    now: Instant
  ): RestaurantUser =
    RestaurantUser.from(restaurant, request.userId, request.position, now)

  private def insertRestaurantUser(restaurantUser: RestaurantUser): ErrorOr[Int] =
    EitherT.liftF(restaurantUserRepository.insert(restaurantUser))
}

object AssignUserToRestaurantService {
  sealed trait AssignUserToRestaurantError extends DomainError
  object AssignUserToRestaurantError {
    case class UserNotFound(message: String = "User not found")                         extends AssignUserToRestaurantError
    case class RestaurantNotFound(message: String = "Restaurant not found")             extends AssignUserToRestaurantError
    case class AlreadyAssigned(message: String = "User already assigned to restaurant") extends AssignUserToRestaurantError
    case class NotManager(message: String = "Only managers can assign users")           extends AssignUserToRestaurantError
  }
}
