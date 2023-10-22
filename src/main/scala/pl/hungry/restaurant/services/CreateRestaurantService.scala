package pl.hungry.restaurant.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, Restaurant, RestaurantUser}
import pl.hungry.restaurant.repositories.{RestaurantRepository, RestaurantUserRepository}
import pl.hungry.restaurant.routers.in.CreateRestaurantRequest
import pl.hungry.restaurant.services.CreateRestaurantService.CreateRestaurantError
import pl.hungry.user.domain.{UserId, UserRole, UserView}
import pl.hungry.user.services.UserInternalService
import pl.hungry.utils.error.DomainError

import java.time.Instant

class CreateRestaurantService(
  userInternalService: UserInternalService,
  restaurantRepository: RestaurantRepository[ConnectionIO],
  restaurantUserRepository: RestaurantUserRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, CreateRestaurantError, T]

  def create(authContext: AuthContext, createRestaurantRequest: CreateRestaurantRequest): IO[Either[CreateRestaurantError, Restaurant]] = {
    val effect = for {
      user <- findUser(authContext.userId)
      _    <- ensureIsAuthorized(user)
      now  <- getTime

      restaurant     = prepareRestaurant(createRestaurantRequest, now)
      restaurantUser = prepareRestaurantUser(restaurant, user.id, now)

      _ <- insertRestaurant(restaurant)
      _ <- insertRestaurantUser(restaurantUser)
    } yield restaurant

    effect.value.transact(transactor)
  }

  private def findUser(userId: UserId): ErrorOr[UserView] =
    EitherT.fromOptionF(userInternalService.find(userId), CreateRestaurantError.UserNotFound())

  private def ensureIsAuthorized(user: UserView): ErrorOr[Unit] =
    EitherT.cond[ConnectionIO](user.role == UserRole.Pro, (), CreateRestaurantError.NotPro())

  private def getTime: ErrorOr[Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def prepareRestaurant(createRestaurantRequest: CreateRestaurantRequest, now: Instant): Restaurant =
    Restaurant.from(createRestaurantRequest, now)

  private def prepareRestaurantUser(
    restaurant: Restaurant,
    userId: UserId,
    now: Instant
  ): RestaurantUser =
    RestaurantUser.from(restaurant, userId, Position.Manager, now)

  private def insertRestaurant(restaurant: Restaurant): ErrorOr[Int] =
    EitherT.liftF(restaurantRepository.insert(restaurant))

  private def insertRestaurantUser(restaurantUser: RestaurantUser): ErrorOr[Int] =
    EitherT.liftF(restaurantUserRepository.insert(restaurantUser))
}

object CreateRestaurantService {
  sealed trait CreateRestaurantError extends DomainError
  object CreateRestaurantError {
    case class UserNotFound(message: String = "User not found")              extends CreateRestaurantError
    case class NotPro(message: String = "Only Pro users can add restaurant") extends CreateRestaurantError
  }
}
