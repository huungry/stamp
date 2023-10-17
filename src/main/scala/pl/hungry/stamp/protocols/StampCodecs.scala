package pl.hungry.stamp.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.stamp.domain.{Stamp, StampId, StampView}
import pl.hungry.stamp.routers.in.CreateStampRequest

object StampCodecs {
  import pl.hungry.restaurant.protocols.RestaurantCodecs._
  import pl.hungry.user.protocols.UserCodecs._
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val stampIdCodec: Codec[StampId]     = deriveUnwrappedCodec
  implicit val stampCodec: Codec[Stamp]         = deriveCodec
  implicit val stampViewCodec: Codec[StampView] = deriveCodec

  implicit val createStampRequestCodec: Codec[CreateStampRequest] = deriveCodec
}
