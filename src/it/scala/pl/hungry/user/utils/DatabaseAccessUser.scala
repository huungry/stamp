package pl.hungry.user.utils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.user.domain.UserEmail
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessUserFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessUser(transactor)
}

class DatabaseAccessUser(xa: Transactor[IO]) extends DatabaseAccess {
  def countActiveUsersByEmail(email: UserEmail): Int =
    sql"SELECT COUNT(*) FROM users where email = ${email.value.value} and archived_at IS NULL"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
