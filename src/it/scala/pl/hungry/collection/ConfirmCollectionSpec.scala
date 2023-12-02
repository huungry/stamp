package pl.hungry.collection

import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.collection.domain.dto.{ConfirmedCollectionDto, UnconfirmedCollectionDto}
import pl.hungry.collection.utils.{DatabaseAccessCollection, DatabaseAccessCollectionFactory}
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.reward.domain.Reward

class ConfirmCollectionSpec extends BaseItTest {

  import pl.hungry.collection.protocols.CollectionCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessCollection](appModules, new DatabaseAccessCollectionFactory)
  }

  it should "not confirm collection by visitor" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward         = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(1)): Unit

    val (createVisitorRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken          = endpoints.login(visitor.email, createVisitorRequest.password)
    endpoints.createStampForUser(visitor.id, restaurant.id, token): Unit

    val unconfirmedCollection: UnconfirmedCollectionDto = endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldDeserializeTo[UnconfirmedCollectionDto]

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections/${unconfirmedCollection.id.value}/confirm",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldIncludeErrorMessage("User is not related with restaurant")

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 1
    db.countUserConfirmedCollections(visitor.id) shouldBe 0
  }

  it should "confirm collection by user related with restaurant (employee/manager)" in new TestCase {
    val (owner, token, restaurant) = endpoints.createUserAndRestaurant()
    val reward: Reward             = endpoints.createRewardForRestaurant(restaurant.id, token)
    endpoints.createStampConfigForRestaurant(NonEmptyList.fromListUnsafe(List(reward)), token, PosInt(1)): Unit

    val (createVisitorRequest, visitor) = endpoints.registerUser()
    val visitorToken: JwtToken          = endpoints.login(visitor.email, createVisitorRequest.password)
    endpoints.createStampForUser(visitor.id, restaurant.id, token): Unit

    val unconfirmedCollection: UnconfirmedCollectionDto = endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections",
        body = "",
        bearerOpt = Some(visitorToken)
      )
      .body
      .shouldDeserializeTo[UnconfirmedCollectionDto]

    val response: ConfirmedCollectionDto = endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/rewards/${reward.id.value}/collections/${unconfirmedCollection.id.value}/confirm",
        body = "",
        bearerOpt = Some(token)
      )
      .body
      .shouldDeserializeTo[ConfirmedCollectionDto]

    response shouldBe ConfirmedCollectionDto(
      id = response.id,
      userId = visitor.id,
      rewardId = reward.id,
      stampsIdUsed = unconfirmedCollection.stampsIdUsed,
      createdAt = unconfirmedCollection.createdAt,
      confirmedBy = owner.id,
      confirmedAt = response.confirmedAt
    )

    assert(response.confirmedAt.isAfter(unconfirmedCollection.createdAt))

    db.countUserUnconfirmedCollections(visitor.id) shouldBe 0
    db.countUserConfirmedCollections(visitor.id) shouldBe 1
  }
}
