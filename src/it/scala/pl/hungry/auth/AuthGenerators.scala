package pl.hungry.auth

import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.Generators
import pl.hungry.auth.routers.in.LoginRequest
import pl.hungry.user.domain._

trait AuthGenerators extends Generators {
  def loginRequestGen: Gen[LoginRequest] = {
    val loginRequestAuxGen: Arbitrary[LoginRequest] = Arbitrary {
      for {
        email    <- emailGen
        password <- nonEmptyStringGen
      } yield LoginRequest(UserEmail(email), PasswordPlain(password))
    }

    loginRequestAuxGen.arbitrary
  }
}
