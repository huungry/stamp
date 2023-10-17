package pl.hungry

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.{Position, RestaurantId}
import pl.hungry.user.domain.UserId

class DatabaseAccess(xa: Transactor[IO]) {
  def upgradeUserToPro(userId: UserId): Unit =
    sql"update users set role ='Pro' where id = ${userId.value}".update.run
      .transact(xa)
      .unsafeRunSync(): Unit

  def findActiveRestaurantUser(userId: UserId): Option[(RestaurantId, UserId, Position)] =
    sql"select restaurant_id, user_id, position from restaurant_user where user_id = ${userId.value} and archived_at IS NULL"
      .query[(RestaurantId, UserId, Position)]
      .option
      .transact(xa)
      .unsafeRunSync()

  def countActiveStamps(restaurantId: RestaurantId, userId: UserId): Int =
    sql"select count(*) from stamp where restaurant_id = ${restaurantId.value} and user_id = ${userId.value} and used_at IS NULL"
      .query[Int]
      .unique
      .transact(xa)
      .unsafeRunSync()
}
