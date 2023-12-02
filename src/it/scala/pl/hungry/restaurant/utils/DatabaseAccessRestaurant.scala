package pl.hungry.restaurant.utils

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.{Position, RestaurantId}
import pl.hungry.user.domain.UserId
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessRestaurantFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessRestaurant(transactor)
}

class DatabaseAccessRestaurant(xa: Transactor[IO]) extends DatabaseAccess {
  def findActiveRestaurantUser(userId: UserId): Option[(RestaurantId, UserId, Position)] =
    sql"SELECT restaurant_id, user_id, position from restaurant_user where user_id = ${userId.value} and archived_at IS NULL"
      .query[(RestaurantId, UserId, Position)]
      .option
      .transact(xa)
      .unsafeRunSync()

  // TODO to remove after payment service is implemented
  def upgradeUserToPro(userId: UserId): Unit =
    sql"UPDATE users SET role ='Pro' WHERE id = ${userId.value}".update.run
      .transact(xa)
      .unsafeRunSync(): Unit
}
