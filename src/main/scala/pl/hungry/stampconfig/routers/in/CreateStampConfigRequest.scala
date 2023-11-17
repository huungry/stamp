package pl.hungry.stampconfig.routers.in

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.reward.domain.RewardId

final case class CreateStampConfigRequest(stampsToReward: PosInt, rewards: NonEmptyList[RewardId])
