package pl.hungry.reward.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.reward.domain.{Reward, RewardId, RewardName}
import pl.hungry.reward.routers.in.CreateRewardRequest

object RewardCodecs {
  import pl.hungry.restaurant.protocols.RestaurantCodecs._
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val rewardIdCodec: Codec[RewardId]                       = deriveUnwrappedCodec
  implicit val rewardNameCodec: Codec[RewardName]                   = deriveUnwrappedCodec
  implicit val rewardCodec: Codec[Reward]                           = deriveCodec
  implicit val createRewardRequestCodec: Codec[CreateRewardRequest] = deriveCodec
}
