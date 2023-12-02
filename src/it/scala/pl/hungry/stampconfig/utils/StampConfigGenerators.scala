package pl.hungry.stampconfig.utils

import cats.data.NonEmptyList
import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.Generators
import pl.hungry.reward.domain.RewardId
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest

import scala.util.Random

trait StampConfigGenerators extends Generators {
  def createStampConfigRequestGen: Gen[CreateStampConfigRequest] = {
    val createStampConfigRequestAuxGen: Arbitrary[CreateStampConfigRequest] = Arbitrary {
      for {
        stampsToReward <- posIntGen
        rewards = NonEmptyList.fromListUnsafe(List.fill(Random.nextInt(3) + 1)(RewardId.generate))
      } yield CreateStampConfigRequest(stampsToReward, rewards)
    }

    createStampConfigRequestAuxGen.arbitrary
  }
}
