package pl.hungry.auth.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import pl.hungry.auth.domain.{RefreshToken, UserRefreshToken}
import pl.hungry.auth.repositories.UserRefreshTokenRepositoryDoobie._
import pl.hungry.user.domain.UserId

final class UserRefreshTokenRepositoryDoobie extends UserRefreshTokenRepository[ConnectionIO] {

  override def find(userId: UserId, refreshToken: RefreshToken): ConnectionIO[Option[UserRefreshToken]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE user_id = ${userId.value} AND refresh_token = ${refreshToken.value}")
      .query[UserRefreshToken]
      .option

  override def upsert(userRefreshToken: UserRefreshToken): ConnectionIO[Int] =
    sql"""INSERT INTO $table ($columns)
          VALUES (
          ${userRefreshToken.userId.value},
          ${userRefreshToken.refreshToken.value},
          ${userRefreshToken.userAgent.value},
          ${userRefreshToken.createdAt},
          ${userRefreshToken.updatedAt})
          ON CONFLICT (user_id, user_agent) DO UPDATE
          SET refresh_token = ${userRefreshToken.refreshToken.value}, updated_at = ${userRefreshToken.updatedAt}
       """.update.run
}

object UserRefreshTokenRepositoryDoobie {
  private val table     = fr"user_refresh_token"
  private val columns   = fr"user_id, refresh_token, user_agent, created_at, updated_at"
  private val fromTable = fr"FROM" ++ table
}
