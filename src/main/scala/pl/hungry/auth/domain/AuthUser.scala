package pl.hungry.auth.domain

import pl.hungry.user.domain.{PasswordHash, UserEmail, UserId}

import java.time.Instant

final case class AuthUser(
  id: UserId,
  email: UserEmail,
  passwordHash: PasswordHash,
  blockedAt: Option[Instant],
  archivedAt: Option[Instant])
