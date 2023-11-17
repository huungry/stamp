package pl.hungry.user.domain

import io.scalaland.chimney.dsl._

import java.time.Instant

final case class UserView(
  id: UserId,
  email: UserEmail,
  firstName: FirstName,
  lastName: LastName,
  nickName: NickName,
  role: UserRole,
  createdAt: Instant,
  blockedAt: Option[Instant],
  archivedAt: Option[Instant])

object UserView {
  def from(user: User): UserView = user.into[UserView].transform
}
