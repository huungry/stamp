package pl.hungry.collection

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.user.domain.UserId
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessCollectionFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessCollection(transactor)
}

class DatabaseAccessCollection(xa: Transactor[IO]) extends DatabaseAccess {
  def countUserUnconfirmedCollections(userId: UserId): Int =
    sql"SELECT COUNT(*) FROM unconfirmed_collection WHERE user_id = ${userId.value}"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
