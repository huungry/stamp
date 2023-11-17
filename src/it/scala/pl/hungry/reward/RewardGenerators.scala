package pl.hungry.reward

import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.Generators
import pl.hungry.reward.domain.RewardName
import pl.hungry.reward.routers.in.CreateRewardRequest

trait RewardGenerators extends Generators {
  def createRewardRequestGen: Gen[CreateRewardRequest] = {
    val createRewardRequestAuxGen: Arbitrary[CreateRewardRequest] = Arbitrary {
      for {
        name  <- nonEmptyStringGen
      } yield CreateRewardRequest(RewardName(name))
    }

    createRewardRequestAuxGen.arbitrary
  }
}
