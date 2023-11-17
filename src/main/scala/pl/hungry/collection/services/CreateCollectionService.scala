package pl.hungry.collection.services

import cats.data.{EitherT, NonEmptyList}
import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.auth.domain.AuthContext
import pl.hungry.collection.domain.UnconfirmedCollection
import pl.hungry.collection.domain.dto.UnconfirmedCollectionDto
import pl.hungry.collection.repositories.UnconfirmedCollectionRepository
import pl.hungry.collection.services.CreateCollectionService.CreateCollectionError
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.domain.{Reward, RewardId}
import pl.hungry.reward.services.RewardInternalService
import pl.hungry.stamp.domain.StampId
import pl.hungry.stamp.services.StampInternalService
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.services.StampConfigInternalService
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

import java.time.Instant

class CreateCollectionService(
  rewardInternalService: RewardInternalService,
  restaurantInternalService: RestaurantInternalService,
  stampConfigInternalService: StampConfigInternalService,
  stampInternalService: StampInternalService,
  unconfirmedCollectionRepository: UnconfirmedCollectionRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, CreateCollectionError, T]

  def create(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    rewardId: RewardId
  ): IO[Either[CreateCollectionError, UnconfirmedCollectionDto]] = {
    val effect = for {
      reward        <- ensureRewardExists(rewardId)
      _             <- ensureRewardMatchRestaurant(reward, restaurantId)
      _             <- ensureRestaurantExists(restaurantId)
      now           <- getTime
      stampConfig   <- findStampConfig(restaurantId, now)
      stampsIdToUse <- listStampsForUpdate(authContext.userId, restaurantId, stampConfig.stampsToReward)
      _             <- markStampsAsUsed(stampsIdToUse, now)
      unconfirmedCollection = createUnconfirmedCollection(authContext.userId, rewardId, stampsIdToUse, now)
      _ <- insertUnconfirmedCollection(unconfirmedCollection)
    } yield UnconfirmedCollectionDto.from(unconfirmedCollection)

    effect.value.transact(transactor)
  }

  private def ensureRewardExists(rewardId: RewardId): ErrorOr[Reward] =
    EitherT.fromOptionF(rewardInternalService.findActive(rewardId), CreateCollectionError.RewardNotFound())

  private def ensureRewardMatchRestaurant(reward: Reward, restaurantId: RestaurantId): ErrorOr[Unit] =
    EitherT.cond[ConnectionIO](reward.restaurantId == restaurantId, (), CreateCollectionError.RewardDoesNotMatchRestaurant())

  private def ensureRestaurantExists(restaurantId: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantInternalService.findActive(restaurantId), CreateCollectionError.RestaurantNotFound())

  private def getTime: ErrorOr[Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def findStampConfig(restaurantId: RestaurantId, now: Instant): ErrorOr[StampConfig] =
    EitherT.fromOptionF(stampConfigInternalService.findCurrent(restaurantId, now), CreateCollectionError.StampConfigNotFound())

  private def listStampsForUpdate(
    userId: UserId,
    restaurantId: RestaurantId,
    limit: PosInt
  ): ErrorOr[NonEmptyList[StampId]] =
    EitherT
      .liftF(stampInternalService.listActiveForUpdate(userId, restaurantId, limit))
      .ensure(CreateCollectionError.NotEnoughStamps())(_.length == limit.value)
      .map(NonEmptyList.fromListUnsafe)
      .leftWiden

  private def markStampsAsUsed(stampsId: NonEmptyList[StampId], now: Instant): ErrorOr[Int] =
    EitherT.liftF(stampInternalService.markAsUsed(stampsId, now))

  private def createUnconfirmedCollection(
    userId: UserId,
    rewardId: RewardId,
    stampsId: NonEmptyList[StampId],
    now: Instant
  ): UnconfirmedCollection = UnconfirmedCollection.from(userId, rewardId, stampsId, now)

  private def insertUnconfirmedCollection(unconfirmedCollection: UnconfirmedCollection): ErrorOr[Int] =
    EitherT.liftF(unconfirmedCollectionRepository.insert(unconfirmedCollection))
}

object CreateCollectionService {
  sealed trait CreateCollectionError extends DomainError
  object CreateCollectionError {
    case class RewardNotFound(message: String = "Reward not found")                               extends CreateCollectionError
    case class RewardDoesNotMatchRestaurant(message: String = "Reward does not match restaurant") extends CreateCollectionError
    case class RestaurantNotFound(message: String = "Restaurant not found")                       extends CreateCollectionError
    case class StampConfigNotFound(message: String = "Restaurant stamp config not found")         extends CreateCollectionError
    case class NotEnoughStamps(message: String = "User has not enough stamps to collect")         extends CreateCollectionError
  }
}
