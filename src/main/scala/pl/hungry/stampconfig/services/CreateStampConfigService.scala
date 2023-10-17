package pl.hungry.stampconfig.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, Restaurant, RestaurantId}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.services.RewardInternalService
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.repositories.StampConfigRepository
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest
import pl.hungry.stampconfig.services.CreateStampConfigService.CreateStampConfigError
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

import java.time.Instant

class CreateStampConfigService(
  restaurantInternalService: RestaurantInternalService,
  rewardInternalService: RewardInternalService,
  stampConfigRepository: StampConfigRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, CreateStampConfigError, T]

  def create(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    createStampConfigRequest: CreateStampConfigRequest
  ): IO[Either[CreateStampConfigError, StampConfig]] = {
    val effect = for {
      _   <- ensureActiveRestaurantExists(restaurantId)
      _   <- ensureUserIsManager(restaurantId, authContext.userId)
      _   <- ensureRequestedRewardsAreActive(createStampConfigRequest, restaurantId)
      now <- getTime
      stampConfig = prepareStampConfig(createStampConfigRequest, restaurantId, now)
      _ <- insert(stampConfig)
    } yield stampConfig

    effect.value.transact(transactor)
  }

  private def ensureActiveRestaurantExists(restaurantId: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantInternalService.findActive(restaurantId), CreateStampConfigError.RestaurantNotFound())

  private def ensureUserIsManager(restaurantId: RestaurantId, userId: UserId): ErrorOr[Unit] =
    for {
      restaurantUser <-
        EitherT.fromOptionF(restaurantInternalService.findRestaurantUser(userId, restaurantId), CreateStampConfigError.RestaurantUserNotFound())
      _ <- EitherT.cond[ConnectionIO](restaurantUser.position == Position.Manager, (), CreateStampConfigError.NotManager(): CreateStampConfigError)
    } yield ()

  private def ensureRequestedRewardsAreActive(createStampConfigRequest: CreateStampConfigRequest, restaurantId: RestaurantId): ErrorOr[Unit] = {
    val requestedRewards = createStampConfigRequest.rewards
    for {
      activeRewards <- EitherT.liftF(rewardInternalService.listActive(restaurantId))
      _ <- EitherT
             .cond[ConnectionIO](requestedRewards.forall(activeRewards.map(_.id).contains), (), CreateStampConfigError.InvalidRewards())
             .leftWiden[CreateStampConfigError]
    } yield ()
  }

  private def getTime: EitherT[ConnectionIO, CreateStampConfigError, Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def prepareStampConfig(
    createStampConfigRequest: CreateStampConfigRequest,
    restaurantId: RestaurantId,
    now: Instant
  ) = StampConfig.from(createStampConfigRequest, restaurantId, now)

  private def insert(stampConfig: StampConfig): ErrorOr[Int] =
    EitherT.liftF(stampConfigRepository.insert(stampConfig))
}

object CreateStampConfigService {
  sealed trait CreateStampConfigError extends DomainError
  object CreateStampConfigError {
    case class RestaurantNotFound(message: String = "Active restaurant not found")             extends CreateStampConfigError
    case class RestaurantUserNotFound(message: String = "User is not related with restaurant") extends CreateStampConfigError
    case class NotManager(message: String = "Only restaurant managers can list rewards")       extends CreateStampConfigError
    case class InvalidRewards(message: String = "Not all rewards are active")                  extends CreateStampConfigError
  }
}
