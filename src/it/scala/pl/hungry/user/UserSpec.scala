package pl.hungry.user

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.user.domain._

class UserSpec extends BaseItTest with UserGenerators {

  import pl.hungry.user.protocols.UserCodecs._

  it should "not create user with the same email" in {
    val (_, existingUser) = endpoints.registerUser()

    val request  = createUserRequestGen.sample.get.copy(email = existingUser.email)
    val response = endpoints.sendPostRequest(path = "http://test.com/accounts/users", body = request.asJson.noSpaces, bearerOpt = None)

    response.body.shouldIncludeErrorMessage("Email already used")
  }

  it should "create user" in {
    val request  = createUserRequestGen.sample.get
    val response = endpoints.sendPostRequest(path = "http://test.com/accounts/users", body = request.asJson.noSpaces, bearerOpt = None)

    val result = response.body.shouldDeserializeTo[UserView]

    result shouldBe UserView(
      id = result.id,
      email = request.email,
      firstName = request.firstName,
      lastName = request.lastName,
      nickName = request.nickName,
      role = UserRole.Basic,
      createdAt = result.createdAt,
      blockedAt = None,
      archivedAt = None
    )
  }
}
