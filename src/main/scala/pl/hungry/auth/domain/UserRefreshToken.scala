package pl.hungry.auth.domain

import pl.hungry.user.domain.UserId

import java.time.Instant

final case class UserRefreshToken(
  userId: UserId,
  refreshToken: RefreshToken,
  userAgent: UserAgent,
  createdAt: Instant,
  updatedAt: Instant)

object UserRefreshToken {
  def from(
    userId: UserId,
    refreshToken: RefreshToken,
    userAgent: UserAgent,
    now: Instant
  ): UserRefreshToken =
    new UserRefreshToken(userId, refreshToken, userAgent, now, now)
}
