package pl.hungry.stampconfig

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.reward.domain.Reward
import pl.hungry.stamp.domain.StampView
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.utils.{DatabaseAccessStampConfig, DatabaseAccessStampConfigFactory}

class ListStampViewSpec extends BaseItTest {

  import pl.hungry.stamp.protocols.StampCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessStampConfig](appModules, new DatabaseAccessStampConfigFactory)
  }

  it should "list users stamps for restaurants" in new TestCase {
    val (_, ownerToken1, restaurant1) = endpoints.createUserAndRestaurant() // stamps config here
    val (_, ownerToken2, restaurant2) = endpoints.createUserAndRestaurant() // no stamps config here
    endpoints.createUserAndRestaurant(): Unit // user will have no stamps here so should not be included in response

    val reward1: Reward           = endpoints.createRewardForRestaurant(restaurant1.id, ownerToken1)
    val stampConfig1: StampConfig = endpoints.createStampConfigForRestaurant(NonEmptyList.one(reward1), ownerToken1, PosInt(4))

    val (createVisitorRequest, visitorView) = endpoints.registerUser()

    // two stamps for restaurant1 and one for restaurant2
    endpoints.createStampForUser(visitorView.id, restaurant1.id, ownerToken1): Unit
    endpoints.createStampForUser(visitorView.id, restaurant1.id, ownerToken1): Unit
    endpoints.createStampForUser(visitorView.id, restaurant2.id, ownerToken2): Unit

    val visitorToken: JwtToken = endpoints.login(visitorView.email, createVisitorRequest.password)

    val response: List[StampView] = endpoints
      .sendGetRequest(path = s"http://test.com/restaurants/stamps", bearerOpt = Some(visitorToken))
      .body
      .shouldDeserializeTo[List[StampView]]

    response should contain theSameElementsInOrderAs List(
      StampView(restaurant1.id, restaurant1.name, Some(stampConfig1.stampsToReward), collectedStamps = PosInt(2)),
      StampView(restaurant2.id, restaurant2.name, None, collectedStamps = PosInt(1))
    )
  }
}
