package pl.hungry.user.utils

import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.Generators
import pl.hungry.user.domain._
import pl.hungry.user.routers.in.CreateUserRequest

trait UserGenerators extends Generators {
  def createUserRequestGen: Gen[CreateUserRequest] = {
    val createUserRequestAuxGen: Arbitrary[CreateUserRequest] = Arbitrary {
      for {
        email     <- emailGen
        password  <- nonEmptyStringGen
        firstName <- nonEmptyStringGen
        lastName  <- nonEmptyStringGen
        nickName  <- nonEmptyStringGen
      } yield CreateUserRequest(UserEmail(email), PasswordPlain(password), FirstName(firstName), LastName(lastName), NickName(nickName))
    }

    createUserRequestAuxGen.arbitrary
  }
}
