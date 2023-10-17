package pl.hungry.auth.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import pl.hungry.auth.domain.AuthUser
import pl.hungry.auth.repositories.AuthRepositoryDoobie._
import pl.hungry.user.domain.{UserEmail, UserId}

final class AuthRepositoryDoobie extends AuthRepository[ConnectionIO] {
  override def find(userId: UserId): ConnectionIO[Option[AuthUser]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE id = ${userId.value}")
      .query[AuthUser]
      .option

  override def find(userEmail: UserEmail): ConnectionIO[Option[AuthUser]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE email = ${userEmail.value.value}")
      .query[AuthUser]
      .option
}

object AuthRepositoryDoobie {
  private val table     = fr"users"
  private val columns   = fr"id, email, password, blocked_at, archived_at"
  private val fromTable = fr"FROM" ++ table
}
