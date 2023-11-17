package pl.hungry.collection

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.collection.domain.dto.UnconfirmedCollectionDto
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.reward.domain.{Reward, RewardId}
import pl.hungry.stamp.domain.Stamp

class CreateCollectionSpec extends BaseItTest {

  import pl.hungry.collection.protocols.CollectionCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup(appModules)
  }

  it should "not create collection when reward does not exists" in new TestCase {
    val (_, _, restaurant) = endpoints.createUserAndRestaurant()

    val visitingUserToken: JwtToken = endpoints.registerAndRetrieveToken()

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${RewardId.generate.value}/collections",
        body = "",
        bearerOpt = Some(visitingUserToken)
      )
      .body
      .shouldIncludeErrorMessage("Reward not found")
  }

  it should "not create collection when reward does not match restaurant" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, _, restaurant2)    = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(3)): Unit

    val visitingUserToken: JwtToken = endpoints.registerAndRetrieveToken()

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant2.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitingUserToken)
      )
      .body
      .shouldIncludeErrorMessage("Reward does not match restaurant")
  }

  it should "not create collection when restaurant stamp config not found" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)

    val visitingUserToken: JwtToken = endpoints.registerAndRetrieveToken()

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitingUserToken)
      )
      .body
      .shouldIncludeErrorMessage("Restaurant stamp config not found")
  }

  it should "not create collection when user does not have enough stamps" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(3)): Unit

    val visitingUserToken: JwtToken = endpoints.registerAndRetrieveToken()

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitingUserToken)
      )
      .body
      .shouldIncludeErrorMessage("User has not enough stamps to collect")
  }

  it should "create collection when user when have enough stamps" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(1)): Unit

    val (createVisitorRequest, visitorView) = endpoints.registerUser()
    val visitorToken: JwtToken              = endpoints.login(visitorView.email, createVisitorRequest.password)
    val stamp: Stamp                        = endpoints.addStampForUser(visitorView.id, restaurant.id, token)

    val response: UnconfirmedCollectionDto = endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldDeserializeTo[UnconfirmedCollectionDto]

    response shouldBe UnconfirmedCollectionDto(
      id = response.id,
      userId = visitorView.id,
      rewardId = reward.id,
      stampsIdUsed = List(stamp.id),
      createdAt = response.createdAt
    )
  }
}
