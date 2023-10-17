package pl.hungry.stamp.repositories

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stamp.domain.{Stamp, StampId, StampView}
import pl.hungry.user.domain.UserId

import java.time.Instant

trait StampRepository[F[_]] {
  def insert(stamp: Stamp): F[Int]
  def listView(userId: UserId, now: Instant): F[List[StampView]]
  def listActiveForUpdate(
    userId: UserId,
    restaurantId: RestaurantId,
    limit: PosInt
  ): F[List[StampId]]
  def markAsUsed(stampsId: NonEmptyList[StampId], now: Instant): F[Int]
}
