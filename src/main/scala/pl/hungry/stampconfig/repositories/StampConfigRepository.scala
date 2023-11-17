package pl.hungry.stampconfig.repositories

import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stampconfig.domain.StampConfig

import java.time.Instant

trait StampConfigRepository[F[_]] {
  def findCurrent(restaurantId: RestaurantId, now: Instant): F[Option[StampConfig]]
  def insert(stampConfig: StampConfig): F[Int]
}
