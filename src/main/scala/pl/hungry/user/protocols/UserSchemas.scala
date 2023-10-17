package pl.hungry.user.protocols

import pl.hungry.user.domain._
import sttp.tapir.Schema

object UserSchemas {
  implicit val userEmailSchema: Schema[UserEmail]    = Schema.string
  implicit val firstNameSchema: Schema[FirstName]    = Schema.string
  implicit val lastNameSchema: Schema[LastName]      = Schema.string
  implicit val nickNameSchema: Schema[NickName]      = Schema.string
  implicit val passwordSchema: Schema[PasswordPlain] = Schema.string
}
