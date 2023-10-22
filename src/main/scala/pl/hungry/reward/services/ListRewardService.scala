package pl.hungry.reward.services

import cats.data.EitherT
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.domain.Reward
import pl.hungry.reward.repositories.RewardRepository
import pl.hungry.reward.services.ListRewardService.ListRewardError
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

class ListRewardService(
  rewardRepository: RewardRepository[ConnectionIO],
  restaurantInternalService: RestaurantInternalService,
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, ListRewardError, T]

  def listActive(authContext: AuthContext, restaurantId: RestaurantId): IO[Either[ListRewardError, List[Reward]]] = {
    val effect = for {
      restaurantUser <- findRestaurantUser(authContext.userId, restaurantId)
      _              <- ensureHasAccess(restaurantUser)
      activeRewards  <- listActiveRewards(restaurantId)
    } yield activeRewards

    effect.value.transact(transactor)
  }

  private def findRestaurantUser(userId: UserId, restaurantId: RestaurantId): ErrorOr[RestaurantUser] =
    EitherT.fromOptionF(restaurantInternalService.findRestaurantUser(userId, restaurantId), ListRewardError.RestaurantUserNotFound())

  private def ensureHasAccess(restaurantUser: RestaurantUser): ErrorOr[Unit] =
    EitherT.cond[ConnectionIO](restaurantUser.position == Position.Manager, (), ListRewardError.NotManager())

  private def listActiveRewards(restaurantId: RestaurantId): ErrorOr[List[Reward]] =
    EitherT.liftF(rewardRepository.listActive(restaurantId))
}

object ListRewardService {
  sealed trait ListRewardError extends DomainError
  object ListRewardError {
    case class RestaurantUserNotFound(message: String = "User is not related with restaurant") extends ListRewardError
    case class NotManager(message: String = "Only restaurant managers can list rewards")       extends ListRewardError
  }
}
