package pl.hungry.restaurant.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.restaurant.domain._
import pl.hungry.restaurant.routers.in.{AssignUserToRestaurantRequest, CreateRestaurantRequest}

object RestaurantCodecs {
  import pl.hungry.user.protocols.UserCodecs._
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val restaurantIdCodec: Codec[RestaurantId]         = deriveUnwrappedCodec
  implicit val restaurantUserIdCodec: Codec[RestaurantUserId] = deriveUnwrappedCodec
  implicit val restaurantNameCodec: Codec[RestaurantName]     = deriveUnwrappedCodec
  implicit val restaurantEmailCodec: Codec[RestaurantEmail]   = deriveUnwrappedCodec
  implicit val restaurantUserCodec: Codec[RestaurantUser]     = deriveCodec
  implicit val restaurantCodec: Codec[Restaurant]             = deriveCodec

  implicit val createRestaurantRequestCodec: Codec[CreateRestaurantRequest]             = deriveCodec
  implicit val assignUserToRestaurantRequestCodec: Codec[AssignUserToRestaurantRequest] = deriveCodec
}
