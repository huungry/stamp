package pl.hungry.collection.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.collection.domain.{CollectionId, ConfirmedCollection, UnconfirmedCollection}
import pl.hungry.collection.repositories.{ConfirmedCollectionRepository, UnconfirmedCollectionRepository}
import pl.hungry.collection.services.ConfirmCollectionService.ConfirmCollectionError
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.domain.{Reward, RewardId}
import pl.hungry.reward.services.RewardInternalService
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

import java.time.Instant

class ConfirmCollectionService(
  confirmedCollectionRepository: ConfirmedCollectionRepository[ConnectionIO],
  rewardInternalService: RewardInternalService,
  restaurantInternalService: RestaurantInternalService,
  unconfirmedCollectionRepository: UnconfirmedCollectionRepository[ConnectionIO],
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, ConfirmCollectionError, T]

  def confirm(authContext: AuthContext, collectionId: CollectionId): IO[Either[ConfirmCollectionError, ConfirmedCollection]] = {
    val effect = for {
      unconfirmedCollection <- findUnconfirmedCollection(collectionId)
      reward                <- ensureRewardExists(unconfirmedCollection.rewardId)
      _                     <- ensureRestaurantUserExists(authContext.userId, reward.restaurantId)
      _                     <- ensureRestaurantExists(reward.restaurantId)
      now                   <- getTime
      confirmedCollection = buildConfirmedCollection(unconfirmedCollection, authContext.userId, now)
      _ <- insertConfirmedCollection(confirmedCollection)
      _ <- deleteUnconfirmedCollection(unconfirmedCollection)
    } yield confirmedCollection

    effect.value.transact(transactor)
  }

  private def findUnconfirmedCollection(collectionId: CollectionId): ErrorOr[UnconfirmedCollection] =
    EitherT.fromOptionF(unconfirmedCollectionRepository.find(collectionId), ConfirmCollectionError.UnconfirmedCollectionNotFound())

  private def ensureRewardExists(rewardId: RewardId): ErrorOr[Reward] =
    EitherT.fromOptionF(rewardInternalService.findActive(rewardId), ConfirmCollectionError.RewardNotFound())

  private def ensureRestaurantUserExists(userId: UserId, restaurantId: RestaurantId): ErrorOr[RestaurantUser] =
    EitherT.fromOptionF(restaurantInternalService.findRestaurantUser(userId, restaurantId), ConfirmCollectionError.RestaurantUserNotFound())

  private def ensureRestaurantExists(restaurantId: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantInternalService.findActive(restaurantId), ConfirmCollectionError.RestaurantNotFound())

  private def getTime: ErrorOr[Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def buildConfirmedCollection(
    unconfirmedCollection: UnconfirmedCollection,
    confirmedBy: UserId,
    now: Instant
  ): ConfirmedCollection = ConfirmedCollection.from(unconfirmedCollection, confirmedBy, now)

  private def insertConfirmedCollection(confirmedCollection: ConfirmedCollection): ErrorOr[Int] =
    EitherT.liftF(confirmedCollectionRepository.insert(confirmedCollection))

  private def deleteUnconfirmedCollection(unconfirmedCollection: UnconfirmedCollection): ErrorOr[Int] =
    EitherT.liftF(unconfirmedCollectionRepository.delete(unconfirmedCollection.id))
}

object ConfirmCollectionService {
  sealed trait ConfirmCollectionError extends DomainError
  object ConfirmCollectionError {
    case class UnconfirmedCollectionNotFound(message: String = "Unconfirmed collection not found") extends ConfirmCollectionError
    case class RewardNotFound(message: String = "Reward not found")                                extends ConfirmCollectionError
    case class RestaurantNotFound(message: String = "Restaurant not found")                        extends ConfirmCollectionError
    case class RestaurantUserNotFound(message: String = "User is not related with restaurant")     extends ConfirmCollectionError
  }
}
