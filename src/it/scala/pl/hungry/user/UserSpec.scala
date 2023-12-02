package pl.hungry.user

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.user.domain._
import pl.hungry.user.utils.{DatabaseAccessUser, DatabaseAccessUserFactory, UserGenerators}

class UserSpec extends BaseItTest with UserGenerators {

  import pl.hungry.user.protocols.UserCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessUser](appModules, new DatabaseAccessUserFactory)
  }

  it should "not create user with the same email" in new TestCase {
    val (_, existingUser) = endpoints.registerUser()

    val request  = createUserRequestGen.sample.get.copy(email = existingUser.email)
    val response = endpoints.sendPostRequest(path = "http://test.com/accounts/users", body = request.asJson.noSpaces, bearerOpt = None)

    response.body.shouldIncludeErrorMessage("Email already used")

    db.countActiveUsersByEmail(existingUser.email) shouldBe 1
  }

  it should "create user" in new TestCase {
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

    db.countActiveUsersByEmail(request.email) shouldBe 1
  }
}
