package pl.hungry.auth

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.auth.utils.{AuthGenerators, DatabaseAccessAuth, DatabaseAccessAuthFactory}
import pl.hungry.main.AppBuilder.AppModules

class AuthSpec extends BaseItTest with AuthGenerators {

  import pl.hungry.auth.protocols.AuthCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessAuth](appModules, new DatabaseAccessAuthFactory)
  }

  it should "not login user with invalid credentials" in new TestCase {
    val (_, registeredUser) = endpoints.registerUser()

    val request = loginRequestGen.sample.get.copy(email = registeredUser.email)
    val result  = endpoints.sendPostRequest(path = "http://test.com/auth/login", body = request.asJson.noSpaces, bearerOpt = None).body

    result.shouldIncludeErrorMessage("Invalid credentials")
  }

  it should "login user with valid credentials" in new TestCase {
    val (createUserRequest, registeredUser) = endpoints.registerUser()

    val request  = LoginRequest(email = registeredUser.email, password = createUserRequest.password)
    val response = endpoints.sendPostRequest(path = "http://test.com/auth/login", body = request.asJson.noSpaces, bearerOpt = None)
    val result   = response.body.shouldDeserializeTo[JwtToken]

    result.token.split('.') should have length 3
  }
}
