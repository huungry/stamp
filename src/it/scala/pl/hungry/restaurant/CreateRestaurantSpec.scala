package pl.hungry.restaurant

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.restaurant.domain.{Position, Restaurant}

class CreateRestaurantSpec extends BaseItTest with RestaurantGenerators {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._

  it should "not create restaurant for basic user" in {
    val (createUserRequest, user) = endpoints.registerUser()
    val token                     = endpoints.login(user.email, createUserRequest.password)

    val request  = createRestaurantRequestGen.sample.get
    val response = endpoints.sendPostRequest(path = "/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token))

    response.body.shouldIncludeErrorMessage("Only Pro users can add restaurant")
    db.findActiveRestaurantUser(user.id) shouldBe None
  }

  it should "create restaurant for pro user with restaurant-user link" in {
    val (createUserRequest, user) = endpoints.registerUser()
    val token                     = endpoints.login(user.email, createUserRequest.password)
    db.upgradeUserToPro(user.id)

    val request  = createRestaurantRequestGen.sample.get
    val response = endpoints.sendPostRequest(path = "http://test.com/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token))
    val result   = response.body.shouldDeserializeTo[Restaurant]

    result shouldBe Restaurant(result.id, request.email, request.name, result.createdAt, None): Unit

    db.findActiveRestaurantUser(user.id).value shouldBe (result.id, user.id, Position.Manager)
  }
}
