package pl.hungry.user.repositories

import pl.hungry.user.domain.{User, UserEmail, UserId}

trait UserRepository[F[_]] {
  def insert(user: User): F[Int]
  def find(id: UserId): F[Option[User]]
  def find(userEmail: UserEmail): F[Option[User]]
}
