package pl.hungry.stampconfig.routers.in

import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.reward.domain.RewardId

final case class CreateStampConfigRequest(stampsToReward: PosInt, rewards: List[RewardId])
