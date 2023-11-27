package pl.hungry.reward

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessRewardFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessReward(transactor)
}

class DatabaseAccessReward(xa: Transactor[IO]) extends DatabaseAccess {
  def countActiveRewards(restaurantId: RestaurantId): Int =
    sql"SELECT COUNT(*) FROM reward where restaurant_id = ${restaurantId.value} and archived_at IS NULL"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
