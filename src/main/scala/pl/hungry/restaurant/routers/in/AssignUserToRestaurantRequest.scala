package pl.hungry.restaurant.routers.in

import pl.hungry.restaurant.domain.Position
import pl.hungry.user.domain.UserId

final case class AssignUserToRestaurantRequest(userId: UserId, position: Position)
