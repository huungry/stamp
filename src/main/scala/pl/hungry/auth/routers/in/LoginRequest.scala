package pl.hungry.auth.routers.in

import pl.hungry.user.domain.{PasswordPlain, UserEmail}

final case class LoginRequest(email: UserEmail, password: PasswordPlain)
