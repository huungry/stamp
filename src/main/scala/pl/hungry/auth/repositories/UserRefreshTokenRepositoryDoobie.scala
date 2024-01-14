package pl.hungry.auth.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import pl.hungry.auth.domain.{RefreshToken, UserRefreshToken}
import pl.hungry.user.domain.UserId

final class UserRefreshTokenRepositoryDoobie extends UserRefreshTokenRepository[ConnectionIO] {

  override def find(userId: UserId, refreshToken: RefreshToken): ConnectionIO[Option[UserRefreshToken]] = ???

  override def upsert(userRefreshToken: UserRefreshToken): ConnectionIO[Int] = ???
}

object UserRefreshTokenRepositoryDoobie {
  private val table     = fr"user_refresh_tokens"
  private val columns   = fr"id, email, password, blocked_at, archived_at"
  private val fromTable = fr"FROM" ++ table
}
