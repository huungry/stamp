package pl.hungry.restaurant.repositories

import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment.Fragment
import doobie.{ConnectionIO, Fragments}
import pl.hungry.restaurant.domain.{Position, RestaurantId, RestaurantUser, RestaurantUserId}
import pl.hungry.restaurant.repositories.RestaurantUserRepositoryDoobie._
import pl.hungry.user.domain.UserId

import java.time.Instant

final class RestaurantUserRepositoryDoobie extends RestaurantUserRepository[ConnectionIO] {

  override def findActive(userId: UserId, restaurantId: RestaurantId): ConnectionIO[Option[RestaurantUser]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments
      .whereAnd(fr"restaurant_id = ${restaurantId.value}", fr"user_id = ${userId.value}", fr"archived_at IS NULL"))
      .query[RestaurantUser]
      .option

  override def findActiveWithPosition(
    userId: UserId,
    restaurantId: RestaurantId,
    position: Position
  ): ConnectionIO[Option[RestaurantUser]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments
      .whereAnd(
        fr"restaurant_id = ${restaurantId.value}",
        fr"user_id = ${userId.value}",
        fr"position = ${position.entryName}",
        fr"archived_at IS NULL"
      ))
      .query[RestaurantUser]
      .option

  override def listActive(userId: UserId): ConnectionIO[List[RestaurantUser]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments
      .whereAnd(fr"user_id = ${userId.value}", fr"archived_at IS NULL"))
      .query[RestaurantUser]
      .to[List]

  override def insert(restaurantUser: RestaurantUser): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
       (${restaurantUser.id.value},
       ${restaurantUser.restaurantId.value},
       ${restaurantUser.userId.value},
       ${restaurantUser.position.entryName},
       ${restaurantUser.createdAt},
       NULL)""".update.run

  override def archive(restaurantUserId: RestaurantUserId, now: Instant): ConnectionIO[Int] =
    sql"UPDATE $table SET archived_at = $now WHERE id = ${restaurantUserId.value}".update.run
}

object RestaurantUserRepositoryDoobie {
  private val table             = fr"restaurant_user"
  private val fromTable         = fr"FROM" ++ table
  private val columns: Fragment = fr"id, restaurant_id, user_id, position, created_at, archived_at"
}
