package pl.hungry.auth

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.auth.domain.JwtToken
import pl.hungry.auth.routers.in.LoginRequest

class AuthSpec extends BaseItTest with AuthGenerators {

  import pl.hungry.auth.protocols.AuthCodecs._

  it should "not login user with invalid credentials" in {
    val (_, registeredUser) = endpoints.registerUser()

    val request = loginRequestGen.sample.get.copy(email = registeredUser.email)
    val result  = endpoints.sendPostRequest(path = "http://test.com/auth/login", body = request.asJson.noSpaces, bearerOpt = None).body

    result.shouldIncludeErrorMessage("Invalid credentials")
  }

  it should "login user with valid credentials" in {
    val (createUserRequest, registeredUser) = endpoints.registerUser()

    val request  = LoginRequest(email = registeredUser.email, password = createUserRequest.password)
    val response = endpoints.sendPostRequest(path = "http://test.com/auth/login", body = request.asJson.noSpaces, bearerOpt = None)
    val result   = response.body.shouldDeserializeTo[JwtToken]

    result.token.split('.') should have length 3
  }
}
