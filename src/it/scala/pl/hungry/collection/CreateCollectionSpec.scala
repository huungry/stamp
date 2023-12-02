package pl.hungry.collection

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.collection.domain.dto.UnconfirmedCollectionDto
import pl.hungry.collection.utils.{DatabaseAccessCollection, DatabaseAccessCollectionFactory}
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.reward.domain.{Reward, RewardId}
import pl.hungry.stamp.domain.Stamp

class CreateCollectionSpec extends BaseItTest {

  import pl.hungry.collection.protocols.CollectionCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessCollection](appModules, new DatabaseAccessCollectionFactory)
  }

  it should "not create collection when reward does not exists" in new TestCase {
    val (_, _, restaurant) = endpoints.createUserAndRestaurant()

    val (createUserRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken       = endpoints.login(createUserRequest.email, createUserRequest.password)

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${RewardId.generate.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldIncludeErrorMessage("Reward not found")

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 0
  }

  it should "not create collection when reward does not match restaurant" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, _, restaurant2)    = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(3)): Unit

    val (createUserRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken       = endpoints.login(createUserRequest.email, createUserRequest.password)

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant2.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldIncludeErrorMessage("Reward does not match restaurant")

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 0
  }

  it should "not create collection when restaurant stamp config not found" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)

    val (createUserRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken       = endpoints.login(createUserRequest.email, createUserRequest.password)

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldIncludeErrorMessage("Restaurant stamp config not found")

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 0
  }

  it should "not create collection when user does not have enough stamps" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(3)): Unit

    val (createUserRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken       = endpoints.login(createUserRequest.email, createUserRequest.password)

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldIncludeErrorMessage("User has not enough stamps to collect")

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 0
  }

  it should "create collection when user when have enough stamps" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(1)): Unit

    val (createVisitorRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken          = endpoints.login(visitor.email, createVisitorRequest.password)
    val stamp: Stamp                    = endpoints.addStampForUser(visitor.id, restaurant.id, token)

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
      userId = visitor.id,
      rewardId = reward.id,
      stampsIdUsed = List(stamp.id),
      createdAt = response.createdAt
    )

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 1
  }
}
