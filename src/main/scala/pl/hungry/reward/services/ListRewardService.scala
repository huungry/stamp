package pl.hungry.reward.services

import cats.data.EitherT
import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.domain.Reward
import pl.hungry.reward.repositories.RewardRepository
import pl.hungry.reward.services.ListRewardService.ListRewardError
import pl.hungry.utils.error.DomainError

class ListRewardService(
  rewardRepository: RewardRepository[ConnectionIO],
  restaurantInternalService: RestaurantInternalService,
  transactor: Transactor[IO]) {

  private type ErrorOr[T] = EitherT[ConnectionIO, ListRewardError, T]

  def listActive(restaurantId: RestaurantId): IO[Either[ListRewardError, List[Reward]]] = {
    val effect = for {
      _             <- findRestaurant(restaurantId)
      activeRewards <- listActiveRewards(restaurantId)
    } yield activeRewards

    effect.value.transact(transactor)
  }

  private def findRestaurant(restaurantId: RestaurantId): ErrorOr[Restaurant] =
    EitherT.fromOptionF(restaurantInternalService.findActive(restaurantId), ListRewardError.RestaurantNotFound())

  private def listActiveRewards(restaurantId: RestaurantId): ErrorOr[List[Reward]] =
    EitherT.liftF(rewardRepository.listActive(restaurantId))
}

object ListRewardService {
  sealed trait ListRewardError extends DomainError
  object ListRewardError {
    case class RestaurantNotFound(message: String = "Restaurant not found") extends ListRewardError
  }
}
