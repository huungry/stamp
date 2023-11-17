package pl.hungry.user.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import pl.hungry.user.domain.{User, UserEmail, UserId}
import pl.hungry.user.repositories.UserRepositoryDoobie._

final class UserRepositoryDoobie extends UserRepository[ConnectionIO] {
  override def insert(user: User): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${user.id.value},
         ${user.email.value.value},
         ${user.passwordHash.value.value},
         ${user.firstName.value.value},
         ${user.lastName.value.value},
         ${user.nickName.value.value},
         ${user.role.entryName},
         ${user.createdAt},
         NULL,
         NULL)""".update.run

  override def find(userEmail: UserEmail): ConnectionIO[Option[User]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE email = ${userEmail.value.value}")
      .query[User]
      .option

  override def find(id: UserId): ConnectionIO[Option[User]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE id = ${id.value}")
      .query[User]
      .option
}

object UserRepositoryDoobie {
  private val table     = fr"users"
  private val fromTable = fr"FROM" ++ table
  private val columns   = fr"id, email, password, first_name, last_name, nick_name, role, created_at, blocked_at, archived_at"
}
