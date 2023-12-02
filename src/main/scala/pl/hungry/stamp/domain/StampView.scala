package pl.hungry.stamp.domain

import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.restaurant.domain.{RestaurantId, RestaurantName}

final case class StampView(
  restaurantId: RestaurantId,
  restaurantName: RestaurantName,
  stampsToReward: Option[PosInt],
  collectedStamps: PosInt)
