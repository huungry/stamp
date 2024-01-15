package pl.hungry.auth.repositories

import pl.hungry.auth.domain.{RefreshToken, UserRefreshToken}
import pl.hungry.user.domain.UserId

trait UserRefreshTokenRepository[F[_]] {
  def find(userId: UserId, refreshToken: RefreshToken): F[Option[UserRefreshToken]]
  def upsert(userRefreshToken: UserRefreshToken): F[Int]
}
