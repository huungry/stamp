package pl.hungry.reward.protocols

import pl.hungry.reward.domain.RewardName
import sttp.tapir.Schema

object RewardSchemas {
  implicit val rewardNameSchema: Schema[RewardName] = Schema.string
}
