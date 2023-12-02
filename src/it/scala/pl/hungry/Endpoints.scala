package pl.hungry

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import eu.timepit.refined.types.numeric.PosInt
import io.circe.syntax._
import pl.hungry.auth.domain.JwtToken
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.restaurant.domain.{Position, Restaurant, RestaurantId, RestaurantUser}
import pl.hungry.restaurant.routers.in.AssignUserToRestaurantRequest
import pl.hungry.restaurant.utils.{DatabaseAccessRestaurant, RestaurantGenerators}
import pl.hungry.reward.domain.Reward
import pl.hungry.reward.utils.RewardGenerators
import pl.hungry.stamp.domain.Stamp
import pl.hungry.stamp.routers.in.CreateStampRequest
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.routers.in.CreateStampConfigRequest
import pl.hungry.stampconfig.utils.StampConfigGenerators
import pl.hungry.user.domain._
import pl.hungry.user.routers.in.CreateUserRequest
import pl.hungry.user.utils.UserGenerators
import sttp.client3.{Response, SttpBackend, UriContext, basicRequest}

class Endpoints(
  backendStub: SttpBackend[IO, Any],
  dbAccessRestaurant: DatabaseAccessRestaurant) // TODO remove db after payment service... endpoints should not depend on db access
    extends UserGenerators
    with RestaurantGenerators
    with RewardGenerators
    with StampConfigGenerators
    with TestSupport {

  import pl.hungry.auth.protocols.AuthCodecs._
  import pl.hungry.restaurant.protocols.RestaurantCodecs._
  import pl.hungry.reward.protocols.RewardCodecs._
  import pl.hungry.stamp.protocols.StampCodecs._
  import pl.hungry.stampconfig.protocols.StampConfigCodecs._
  import pl.hungry.user.protocols.UserCodecs._

  def sendPostRequest(
    path: String,
    body: String,
    bearerOpt: Option[JwtToken]
  ): Response[Either[String, String]] =
    bearerOpt match {
      case Some(bearer) =>
        basicRequest
          .post(uri"$path")
          .header("Authorization", s"Bearer ${bearer.token}")
          .body(body)
          .send(backendStub)
          .unsafeRunSync()

      case None =>
        basicRequest
          .post(uri"$path")
          .body(body)
          .send(backendStub)
          .unsafeRunSync()
    }

  def sendGetRequest(path: String, bearerOpt: Option[JwtToken]): Response[Either[String, String]] =
    bearerOpt match {
      case Some(bearer) =>
        basicRequest
          .get(uri"$path")
          .header("Authorization", s"Bearer ${bearer.token}")
          .send(backendStub)
          .unsafeRunSync()

      case None =>
        basicRequest
          .get(uri"$path")
          .send(backendStub)
          .unsafeRunSync()
    }

  def sendDeleteRequest(path: String, bearerOpt: Option[JwtToken]): Response[Either[String, String]] =
    bearerOpt match {
      case Some(bearer) =>
        basicRequest
          .delete(uri"$path")
          .header("Authorization", s"Bearer ${bearer.token}")
          .send(backendStub)
          .unsafeRunSync()

      case None =>
        basicRequest
          .delete(uri"$path")
          .send(backendStub)
          .unsafeRunSync()
    }

  def registerUser(): (CreateUserRequest, UserView) = {
    val request = createUserRequestGen.sample.get
    val userView =
      sendPostRequest(path = "http://test.com/accounts/users", body = request.asJson.noSpaces, bearerOpt = None).body.shouldDeserializeTo[UserView]

    (request, userView)
  }

  def login(email: UserEmail, password: PasswordPlain): JwtToken = {
    val request = LoginRequest(email = email, password = password)
    sendPostRequest(path = "http://test.com/auth/login", body = request.asJson.noSpaces, bearerOpt = None).body
      .shouldDeserializeTo[JwtToken]
  }

  def registerAndRetrieveToken(): JwtToken = {
    val (request, _) = registerUser()
    login(request.email, request.password)
  }

  def createUserAndRestaurant(): (UserView, JwtToken, Restaurant) = {
    val (createUserRequest, userView) = registerUser()
    val token                         = login(userView.email, createUserRequest.password)
    dbAccessRestaurant.upgradeUserToPro(userView.id)

    val request = createRestaurantRequestGen.sample.get
    val restaurant = sendPostRequest(path = "http://test.com/restaurants", body = request.asJson.noSpaces, bearerOpt = Some(token)).body
      .shouldDeserializeTo[Restaurant]

    (userView, token, restaurant)
  }

  def assignUserToRestaurant(
    token: JwtToken,
    forUserId: UserId,
    restaurantId: RestaurantId,
    position: Position
  ): RestaurantUser = {
    val request = AssignUserToRestaurantRequest(userId = forUserId, position = position)
    sendPostRequest(path = s"http://test.com/restaurants/${restaurantId.value}/users", body = request.asJson.noSpaces, bearerOpt = Some(token)).body
      .shouldDeserializeTo[RestaurantUser]
  }

  def createStampForUser(
    userId: UserId,
    restaurantId: RestaurantId,
    creatorToken: JwtToken
  ): Stamp = {
    val request = CreateStampRequest(forUserId = userId)

    sendPostRequest(
      path = s"http://test.com/restaurants/${restaurantId.value}/stamps",
      body = request.asJson.noSpaces,
      bearerOpt = Some(creatorToken)
    ).body
      .shouldDeserializeTo[Stamp]
  }

  def createRewardForRestaurant(restaurantId: RestaurantId, creatorToken: JwtToken): Reward = {
    val request = createRewardRequestGen.sample.get

    sendPostRequest(
      path = s"http://test.com/restaurants/${restaurantId.value}/rewards",
      body = request.asJson.noSpaces,
      bearerOpt = Some(creatorToken)
    ).body
      .shouldDeserializeTo[Reward]
  }

  def createStampConfigForRestaurant(
    rewards: NonEmptyList[Reward],
    creatorToken: JwtToken,
    stampsToReward: PosInt
  ): StampConfig = {
    val request: CreateStampConfigRequest = CreateStampConfigRequest(rewards = rewards.map(_.id), stampsToReward = stampsToReward)

    sendPostRequest(
      path = s"http://test.com/restaurants/${rewards.head.restaurantId.value}/stamps/configs",
      body = request.asJson.noSpaces,
      bearerOpt = Some(creatorToken)
    ).body
      .shouldDeserializeTo[StampConfig]
  }
}
