package pl.hungry.auth.domain

import pl.hungry.user.domain.UserId

final case class JwtToken(token: String) extends AnyVal
final private[auth] case class JwtContent(userId: UserId)
