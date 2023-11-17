package pl.hungry.stampconfig

import cats.data.NonEmptyList
import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.{Position, RestaurantId, RestaurantUser}
import pl.hungry.reward.domain.Reward
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest

class CreateStampConfigSpec extends BaseItTest with StampConfigGenerators {

  import pl.hungry.stampconfig.protocols.StampConfigCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup(appModules)
  }

  it should "not create stamps config for not existing restaurant" in new TestCase {
    val token: JwtToken                   = endpoints.registerAndRetrieveToken()
    val request: CreateStampConfigRequest = createStampConfigRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${RestaurantId.generate.value}/stamps/configs",
        body = request.asJson.noSpaces,
        bearerOpt = Some(token)
      )
      .body
      .shouldIncludeErrorMessage("Active restaurant not found")
  }

  it should "not create stamps config when user is not related with restaurant" in new TestCase {
    val (_, _, restaurant)                = endpoints.createUserAndRestaurant()
    val notRelatedUserToken: JwtToken     = endpoints.registerAndRetrieveToken()
    val request: CreateStampConfigRequest = createStampConfigRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/stamps/configs",
        body = request.asJson.noSpaces,
        bearerOpt = Some(notRelatedUserToken)
      )
      .body
      .shouldIncludeErrorMessage("User is not related with restaurant")

    db.countStampsConfig(restaurant.id) shouldBe 0
  }

  it should "not create stamps config when user is not manager" in new TestCase {
    val (_, ownerToken, restaurant)       = endpoints.createUserAndRestaurant()
    val (createEmployeeRequest, employee) = endpoints.registerUser()
    val employeeToken: JwtToken           = endpoints.login(employee.email, createEmployeeRequest.password)
    val restaurantUser: RestaurantUser    = endpoints.assignUserToRestaurant(ownerToken, employee.id, restaurant.id, Position.Employee)

    db.findActiveRestaurantUser(employee.id).value shouldBe (restaurant.id, employee.id, restaurantUser.position)

    val request: CreateStampConfigRequest = createStampConfigRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/stamps/configs",
        body = request.asJson.noSpaces,
        bearerOpt = Some(employeeToken)
      )
      .body
      .shouldIncludeErrorMessage("Only restaurant managers can list rewards")

    db.countStampsConfig(restaurant.id) shouldBe 0
  }

  it should "not create stamps config when requested rewards does not exists" in new TestCase {
    val (_, ownerToken, restaurant)       = endpoints.createUserAndRestaurant()
    val request: CreateStampConfigRequest = createStampConfigRequestGen.sample.get

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/stamps/configs",
        body = request.asJson.noSpaces,
        bearerOpt = Some(ownerToken)
      )
      .body
      .shouldIncludeErrorMessage("Not all rewards are active")

    db.countStampsConfig(restaurant.id) shouldBe 0
  }

  it should "not create stamps config by restaurant manager" in new TestCase {
    val (_, ownerToken, restaurant)       = endpoints.createUserAndRestaurant()
    val reward: Reward                    = endpoints.createRewardForRestaurant(restaurant.id, ownerToken)
    val request: CreateStampConfigRequest = createStampConfigRequestGen.sample.get.copy(rewards = NonEmptyList.fromListUnsafe(List(reward.id)))

    val response: StampConfig = endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/stamps/configs",
        body = request.asJson.noSpaces,
        bearerOpt = Some(ownerToken)
      )
      .body
      .shouldDeserializeTo[StampConfig]

    response shouldBe StampConfig(
      id = response.id,
      restaurantId = restaurant.id,
      stampsToReward = request.stampsToReward,
      rewards = List(reward.id),
      createdAt = response.createdAt,
      archivedAt = None
    )

    db.countStampsConfig(restaurant.id) shouldBe 1
  }
}
