package pl.hungry.reward

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.Position
import pl.hungry.reward.domain.Reward
import pl.hungry.reward.routers.in.CreateRewardRequest
import pl.hungry.reward.utils.{DatabaseAccessReward, DatabaseAccessRewardFactory, RewardGenerators}

class CreateRewardSpec extends BaseItTest with RewardGenerators {

  import pl.hungry.reward.protocols.RewardCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessReward](appModules, new DatabaseAccessRewardFactory)
  }

  it should "not create reward by not existing restaurant-user" in new TestCase {
    val (_, _, restaurant)            = endpoints.createUserAndRestaurant()
    val nonRelatedUserToken: JwtToken = endpoints.registerAndRetrieveToken()
    val request: CreateRewardRequest  = createRewardRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards",
        body = request.asJson.noSpaces,
        bearerOpt = Some(nonRelatedUserToken)
      )
      .body
      .shouldIncludeErrorMessage("User is not related with restaurant")

    db.countActiveRewards(restaurant.id) shouldBe 0
  }

  it should "not create reward by employee" in new TestCase {
    val (_, token, restaurant)            = endpoints.createUserAndRestaurant()
    val (createEmployeeRequest, employee) = endpoints.registerUser()
    val employeeToken: JwtToken           = endpoints.login(employee.email, createEmployeeRequest.password)
    endpoints.assignUserToRestaurant(token, employee.id, restaurant.id, Position.Employee): Unit

    val request: CreateRewardRequest = createRewardRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards",
        body = request.asJson.noSpaces,
        bearerOpt = Some(employeeToken)
      )
      .body
      .shouldIncludeErrorMessage("Only restaurant managers can add rewards")

    db.countActiveRewards(restaurant.id) shouldBe 0
  }

  it should "not create reward if reward name already exists" in new TestCase {
    val (_, token, restaurant)        = endpoints.createUserAndRestaurant()
    val alreadyExistingReward: Reward = endpoints.createRewardForRestaurant(restaurant.id, token)
    val request: CreateRewardRequest  = CreateRewardRequest(alreadyExistingReward.name)

    endpoints
      .sendPostRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/rewards", body = request.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldIncludeErrorMessage("Reward with given name already exists")

    db.countActiveRewards(restaurant.id) shouldBe 1
  }

  it should "create reward for restaurant by manager" in new TestCase {
    val (_, token, restaurant)       = endpoints.createUserAndRestaurant()
    val request: CreateRewardRequest = createRewardRequestGen.sample.get

    val response: Reward = endpoints
      .sendPostRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/rewards", body = request.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldDeserializeTo[Reward]

    response shouldBe Reward(id = response.id, restaurantId = restaurant.id, name = request.name, archivedAt = None)

    db.countActiveRewards(restaurant.id) shouldBe 1
  }
}
