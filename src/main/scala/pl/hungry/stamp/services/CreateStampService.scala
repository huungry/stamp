package pl.hungry.stamp.services

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Clock
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{RestaurantId, RestaurantUser}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.stamp.domain._
import pl.hungry.stamp.repositories.StampRepository
import pl.hungry.stamp.routers.in.CreateStampRequest
import pl.hungry.stamp.services.CreateStampService.CreateStampError
import pl.hungry.user.domain.{UserId, UserView}
import pl.hungry.user.services.UserInternalService
import pl.hungry.utils.error.DomainError

import java.time.Instant

class CreateStampService(
  stampRepository: StampRepository[ConnectionIO],
  restaurantInternalService: RestaurantInternalService,
  userInternalService: UserInternalService,
  transactor: Transactor[IO]) {

  def create(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    request: CreateStampRequest
  ): IO[Either[CreateStampError, Stamp]] = {
    val effect = for {
      _   <- ensureNotSelfRequest(authContext.userId, request)
      _   <- ensureRestaurantUserExists(authContext.userId, restaurantId)
      _   <- ensureRequestedUserExists(request)
      now <- getTime
      stamp = prepareStamp(restaurantId, request, now)
      _ <- insert(stamp)
    } yield stamp

    effect.value.transact(transactor)
  }

  private def ensureNotSelfRequest(userId: UserId, request: CreateStampRequest): EitherT[ConnectionIO, CreateStampError, Unit] =
    EitherT.cond[ConnectionIO](userId != request.forUserId, (), CreateStampError.UserRequestedThemself())

  private def ensureRestaurantUserExists(userId: UserId, restaurantId: RestaurantId): EitherT[ConnectionIO, CreateStampError, RestaurantUser] =
    EitherT.fromOptionF(restaurantInternalService.findRestaurantUser(userId, restaurantId), CreateStampError.RestaurantUserNotFound())

  private def ensureRequestedUserExists(request: CreateStampRequest): EitherT[ConnectionIO, CreateStampError, UserView] =
    EitherT.fromOptionF(userInternalService.find(request.forUserId), CreateStampError.ClientNotFound())

  private def getTime: EitherT[ConnectionIO, CreateStampError, Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def prepareStamp(
    restaurantId: RestaurantId,
    request: CreateStampRequest,
    now: Instant
  ) =
    Stamp.from(restaurantId, request, now)

  private def insert(stamp: Stamp): EitherT[ConnectionIO, CreateStampError, Int] =
    EitherT.liftF(stampRepository.insert(stamp))
}

object CreateStampService {
  sealed trait CreateStampError extends DomainError
  object CreateStampError {
    case class RestaurantUserNotFound(message: String = "User is not related to restaurant")    extends CreateStampError
    case class UserRequestedThemself(message: String = "User cannot add stamps for themselves") extends CreateStampError
    case class ClientNotFound(message: String = "Client not found")                             extends CreateStampError
  }
}
