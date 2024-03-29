package pl.hungry.restaurant

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.{Position, Restaurant}
import pl.hungry.restaurant.utils.{DatabaseAccessRestaurant, DatabaseAccessRestaurantFactory, RestaurantGenerators}

class CreateRestaurantSpec extends BaseItTest with RestaurantGenerators {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessRestaurant](appModules, new DatabaseAccessRestaurantFactory)
  }

  it should "create first restaurant for basic user" in new TestCase {
    val (createUserRequest, user) = endpoints.registerUser()
    val token                     = endpoints.login(user.email, createUserRequest.password)

    val request  = createRestaurantRequestGen.sample.get
    val response = endpoints.sendPostRequest(path = "/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token))
    val result   = response.body.shouldDeserializeTo[Restaurant]
    result shouldBe Restaurant(result.id, request.email, request.name, user.id, result.createdAt, None)

    db.findActiveRestaurantUser(user.id).value shouldBe (result.id, user.id, Position.Manager)
  }

  it should "not create second restaurant for basic user" in new TestCase {
    val (createUserRequest, user) = endpoints.registerUser()
    val token                     = endpoints.login(user.email, createUserRequest.password)

    val request = createRestaurantRequestGen.sample.get
    endpoints
      .sendPostRequest(path = "/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldDeserializeTo[Restaurant]: Unit
    val response =
      endpoints.sendPostRequest(path = "/restaurants", body = createRestaurantRequestGen.sample.get.asJson.noSpaces, bearerOpt = Some(token))

    response.body.shouldIncludeErrorMessage("Upgrade account to Pro to add more restaurants")
  }

  it should "create multiple restaurants for pro user with restaurant-user link" in new TestCase {
    val (createUserRequest, user) = endpoints.registerUser()
    val token                     = endpoints.login(user.email, createUserRequest.password)
    db.upgradeUserToPro(user.id)

    val request  = createRestaurantRequestGen.sample.get
    val response = endpoints.sendPostRequest(path = "http://test.com/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token))
    val result   = response.body.shouldDeserializeTo[Restaurant]

    result shouldBe Restaurant(result.id, request.email, request.name, user.id, result.createdAt, None)

    db.findActiveRestaurantUser(user.id).value shouldBe (result.id, user.id, Position.Manager)

    endpoints
      .sendPostRequest(path = "http://test.com/restaurants", body = createRestaurantRequestGen.sample.get.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldDeserializeTo[Restaurant]: Unit
    endpoints
      .sendPostRequest(path = "http://test.com/restaurants", body = createRestaurantRequestGen.sample.get.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldDeserializeTo[Restaurant]: Unit
  }
}
