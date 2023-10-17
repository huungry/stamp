package pl.hungry.user.domain

import io.scalaland.chimney.dsl._
import pl.hungry.user.routers.in.CreateUserRequest

import java.time.Instant

final case class User(
  id: UserId,
  email: UserEmail,
  passwordHash: PasswordHash,
  firstName: FirstName,
  lastName: LastName,
  nickName: NickName,
  role: UserRole,
  createdAt: Instant,
  blockedAt: Option[Instant],
  archivedAt: Option[Instant])

object User {
  def from(
    request: CreateUserRequest,
    passwordHash: PasswordHash,
    now: Instant
  ): User =
    request
      .into[User]
      .enableOptionDefaultsToNone
      .withFieldConst(_.id, UserId.generate)
      .withFieldConst(_.passwordHash, passwordHash)
      .withFieldConst(_.role, UserRole.Basic)
      .withFieldConst(_.createdAt, now)
      .transform
}
