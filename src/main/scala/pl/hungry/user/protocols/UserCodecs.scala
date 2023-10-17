package pl.hungry.user.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.user.domain._
import pl.hungry.user.routers.in.CreateUserRequest

object UserCodecs {
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val userIdCodec: Codec[UserId]               = deriveUnwrappedCodec
  implicit val firstNameCodec: Codec[FirstName]         = deriveUnwrappedCodec
  implicit val lastNameCodec: Codec[LastName]           = deriveUnwrappedCodec
  implicit val nickNameCodec: Codec[NickName]           = deriveUnwrappedCodec
  implicit val userEmailCodec: Codec[UserEmail]         = deriveUnwrappedCodec
  implicit val passwordPlainCodec: Codec[PasswordPlain] = deriveUnwrappedCodec
  implicit val passwordHashCodec: Codec[PasswordHash]   = deriveUnwrappedCodec

  implicit val userCodec: Codec[User]         = deriveCodec
  implicit val userViewCodec: Codec[UserView] = deriveCodec

  implicit val createUserRequestCodec: Codec[CreateUserRequest] = deriveCodec
}
