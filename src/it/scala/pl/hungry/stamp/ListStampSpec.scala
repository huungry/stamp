package pl.hungry.stamp

import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.stamp.domain.StampView

class ListStampSpec extends BaseItTest {

  import pl.hungry.stamp.protocols.StampCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup(appModules)
  }

  it should "list user's stamps" in new TestCase {
    val (_, token, restaurant)                        = endpoints.createUserAndRestaurant()
    val (visitingUserCreateRequest, visitingUserView) = endpoints.registerUser()
    endpoints.addStampForUser(visitingUserView.id, restaurant.id, token): Unit

    val visitingUserJwtToken: JwtToken = endpoints.login(visitingUserView.email, visitingUserCreateRequest.password)

    val response: List[StampView] = endpoints
      .sendGetRequest(path = s"http://test.com/restaurants/stamps", bearerOpt = Some(visitingUserJwtToken))
      .body
      .shouldDeserializeTo[List[StampView]]

    response shouldBe List(StampView(restaurant.id, restaurant.name, None, PosInt(1)))
  }
}
