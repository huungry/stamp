package pl.hungry.auth.repositories

import pl.hungry.auth.domain.AuthUser
import pl.hungry.user.domain.{UserEmail, UserId}

trait AuthRepository[F[_]] {
  def find(userId: UserId): F[Option[AuthUser]]
  def find(userEmail: UserEmail): F[Option[AuthUser]]
}
