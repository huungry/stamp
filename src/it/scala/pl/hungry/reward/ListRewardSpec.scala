package pl.hungry.reward

import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.Reward

class ListRewardSpec extends BaseItTest with RewardGenerators {

  import pl.hungry.reward.protocols.RewardCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessReward](appModules, new DatabaseAccessRewardFactory)
  }

  it should "not list rewards for non existing restaurant" in new TestCase {
    val jwtToken: JwtToken = endpoints.registerAndRetrieveToken()

    endpoints
      .sendGetRequest(path = s"http://test.com/restaurants/${RestaurantId.generate.value}/rewards", bearerOpt = Some(jwtToken))
      .body
      .shouldIncludeErrorMessage("Restaurant not found")
  }

  it should "list rewards for restaurant for owner" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)

    val response: List[Reward] = endpoints
      .sendGetRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/rewards", bearerOpt = Some(token))
      .body
      .shouldDeserializeTo[List[Reward]]

    response shouldBe List(reward)
  }

  it should "list rewards for user not connected with restaurant" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)

    val clientToken: JwtToken = endpoints.registerAndRetrieveToken()

    val response: List[Reward] = endpoints
      .sendGetRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/rewards", bearerOpt = Some(clientToken))
      .body
      .shouldDeserializeTo[List[Reward]]

    response shouldBe List(reward)
  }
}
