package pl.hungry.stamp

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.user.domain.UserId
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessStampFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessStamp(transactor)
}

class DatabaseAccessStamp(xa: Transactor[IO]) extends DatabaseAccess {
  def countActiveStamps(restaurantId: RestaurantId, userId: UserId): Int =
    sql"SELECT COUNT(*) FROM stamp where restaurant_id = ${restaurantId.value} and user_id = ${userId.value} and used_at IS NULL"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
