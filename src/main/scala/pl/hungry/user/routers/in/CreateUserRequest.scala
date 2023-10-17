package pl.hungry.user.routers.in

import pl.hungry.user.domain._

final case class CreateUserRequest(
  email: UserEmail,
  password: PasswordPlain,
  firstName: FirstName,
  lastName: LastName,
  nickName: NickName)
