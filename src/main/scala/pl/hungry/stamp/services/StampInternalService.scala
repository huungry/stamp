package pl.hungry.stamp.services

import cats.data.NonEmptyList
import doobie.ConnectionIO
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stamp.domain.StampId
import pl.hungry.stamp.repositories.StampRepository
import pl.hungry.user.domain.UserId

import java.time.Instant

class StampInternalService(stampRepository: StampRepository[ConnectionIO]) {
  def listActiveForUpdate(
    userId: UserId,
    restaurantId: RestaurantId,
    limit: PosInt
  ): ConnectionIO[List[StampId]] = stampRepository.listActiveForUpdate(userId, restaurantId, limit)

  def markAsUsed(stampsId: NonEmptyList[StampId], now: Instant): ConnectionIO[Int] = stampRepository.markAsUsed(stampsId, now)
}
