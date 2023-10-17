package pl.hungry.stamp

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.stamp.domain.Stamp
import pl.hungry.stamp.routers.in.CreateStampRequest
import pl.hungry.user.domain.UserId

class CreateStampSpec extends BaseItTest {

  import pl.hungry.stamp.protocols.StampCodecs._

  it should "not create stamp for not existing user" in {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val nonExistingUserId      = UserId.generate
    val request                = CreateStampRequest(forUserId = nonExistingUserId)

    endpoints
      .sendPostRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/stamps", body = request.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldIncludeErrorMessage("Client not found")
    db.countActiveStamps(restaurant.id, nonExistingUserId) shouldBe 0
  }

  it should "not create stamp for themself" in {
    val (userView, token, restaurant) = endpoints.createUserAndRestaurant()

    val request = CreateStampRequest(forUserId = userView.id)
    endpoints
      .sendPostRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/stamps", body = request.asJson.noSpaces, bearerOpt = Some(token))
      .body
      .shouldIncludeErrorMessage("User cannot add stamps for themselves")
    db.countActiveStamps(restaurant.id, userView.id) shouldBe 0
  }

  it should "not create stamp when creator is not related to restaurant" in {
    val (_, _, restaurant)  = endpoints.createUserAndRestaurant()
    val notRelatedUserToken = endpoints.registerAndRetrieveToken()
    val (_, visitingUser)   = endpoints.registerUser()

    val request = CreateStampRequest(forUserId = visitingUser.id)
    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/stamps",
        body = request.asJson.noSpaces,
        bearerOpt = Some(notRelatedUserToken)
      )
      .body
      .shouldIncludeErrorMessage("User is not related to restaurant")
    db.countActiveStamps(restaurant.id, visitingUser.id) shouldBe 0
  }

  it should "create stamp for user" in {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, visitingUserView)  = endpoints.registerUser()

    val response = endpoints.addStampForUser(visitingUserView.id, restaurant.id, token)

    response shouldBe Stamp(
      id = response.id,
      restaurantId = restaurant.id,
      userId = visitingUserView.id,
      createdAt = response.createdAt,
      usedAt = None
    ): Unit
    db.countActiveStamps(restaurant.id, visitingUserView.id) shouldBe 1
  }
}
