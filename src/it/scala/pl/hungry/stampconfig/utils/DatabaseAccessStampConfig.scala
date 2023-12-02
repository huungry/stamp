package pl.hungry.stampconfig.utils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessStampConfigFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessStampConfig(transactor)
}

class DatabaseAccessStampConfig(xa: Transactor[IO]) extends DatabaseAccess {
  def countStampsConfig(restaurantId: RestaurantId): Int =
    sql"SELECT COUNT(*) FROM stamp_config where restaurant_id = ${restaurantId.value} and archived_at IS NULL"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
