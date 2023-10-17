package pl.hungry.reward.services

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.restaurant.domain.{Position, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.services.RestaurantInternalService
import pl.hungry.reward.domain.{Reward, RewardName}
import pl.hungry.reward.repositories.RewardRepository
import pl.hungry.reward.routers.in.CreateRewardRequest
import pl.hungry.reward.services.CreateRewardService.CreateRewardError
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

class CreateRewardService(
  rewardRepository: RewardRepository[ConnectionIO],
  restaurantInternalService: RestaurantInternalService,
  transactor: Transactor[IO]) {
  def create(
    authContext: AuthContext,
    restaurantId: RestaurantId,
    createRewardRequest: CreateRewardRequest
  ): IO[Either[CreateRewardError, Reward]] = {
    val effect = for {
      restaurantUser <- findRestaurantUser(authContext.userId, restaurantId)
      _              <- ensureHasAccess(restaurantUser)
      _              <- ensureNameNotExists(createRewardRequest.name, restaurantId)
      reward = Reward.from(createRewardRequest, restaurantId)
      _ <- insert(reward)
    } yield reward

    effect.value.transact(transactor)
  }

  private def findRestaurantUser(userId: UserId, restaurantId: RestaurantId): EitherT[ConnectionIO, CreateRewardError, RestaurantUser] =
    EitherT.fromOptionF(restaurantInternalService.findRestaurantUser(userId, restaurantId), CreateRewardError.RestaurantUserNotFound())

  private def ensureHasAccess(restaurantUser: RestaurantUser): EitherT[ConnectionIO, CreateRewardError, Unit] =
    EitherT.cond[ConnectionIO](restaurantUser.position == Position.Manager, (), CreateRewardError.NotManager())

  private def ensureNameNotExists(name: RewardName, restaurantId: RestaurantId): EitherT[ConnectionIO, CreateRewardError, Unit] =
    EitherT {
      rewardRepository.findActive(name, restaurantId).map {
        case Some(_) => CreateRewardError.NameAlreadyExists().asLeft
        case None    => ().asRight
      }
    }

  private def insert(reward: Reward): EitherT[ConnectionIO, CreateRewardError, Int] =
    EitherT.liftF(rewardRepository.insert(reward))
}

object CreateRewardService {
  sealed trait CreateRewardError extends DomainError
  object CreateRewardError {
    case class RestaurantUserNotFound(message: String = "User is not related with restaurant") extends CreateRewardError
    case class NotManager(message: String = "Only restaurant managers can add rewards")        extends CreateRewardError
    case class NameAlreadyExists(message: String = "Reward with given name already exists")    extends CreateRewardError
  }
}
