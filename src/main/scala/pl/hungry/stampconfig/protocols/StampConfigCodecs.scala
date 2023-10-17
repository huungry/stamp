package pl.hungry.stampconfig.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.stampconfig.domain.{StampConfig, StampConfigId}
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest

object StampConfigCodecs {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._
  import pl.hungry.reward.protocols.RewardCodecs._
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val stampConfigIdCodec: Codec[StampConfigId] = deriveUnwrappedCodec
  implicit val stampConfigCodec: Codec[StampConfig]     = deriveCodec

  implicit val createStampConfigRequestCodec: Codec[CreateStampConfigRequest] = deriveCodec
}
