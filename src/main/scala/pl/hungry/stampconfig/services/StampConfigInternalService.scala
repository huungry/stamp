package pl.hungry.stampconfig.services

import doobie.ConnectionIO
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.repositories.StampConfigRepository

import java.time.Instant

class StampConfigInternalService(stampConfigRepository: StampConfigRepository[ConnectionIO]) {
  def findCurrent(restaurantId: RestaurantId, now: Instant): ConnectionIO[Option[StampConfig]] = stampConfigRepository.findCurrent(restaurantId, now)
}
