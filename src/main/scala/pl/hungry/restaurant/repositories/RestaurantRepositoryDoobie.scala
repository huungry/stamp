package pl.hungry.restaurant.repositories

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import doobie.util.fragment.Fragment
import doobie.{ConnectionIO, Fragments}
import pl.hungry.restaurant.domain.{Restaurant, RestaurantId}
import pl.hungry.restaurant.repositories.RestaurantRepositoryDoobie._
import pl.hungry.user.domain.UserId

final class RestaurantRepositoryDoobie extends RestaurantRepository[ConnectionIO] {

  override def countActive(userId: UserId): ConnectionIO[Int] =
    (fr"SELECT COUNT(*)" ++ fromTable ++ Fragments.whereAnd(fr"created_by = ${userId.value}", fr"archived_at IS NULL"))
      .query[Int]
      .unique

  override def findActive(id: RestaurantId): ConnectionIO[Option[Restaurant]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(fr"id = ${id.value}", fr"archived_at IS NULL"))
      .query[Restaurant]
      .option

  override def insert(restaurant: Restaurant): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${restaurant.id.value},
         ${restaurant.email.value.value},
         ${restaurant.name.value.value},
         ${restaurant.createdBy.value},
         ${restaurant.createdAt},
         NULL)""".update.run

  def list(ids: NonEmptyList[RestaurantId]): ConnectionIO[List[Restaurant]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(Fragments.in(fr"id", ids.map(_.value)), fr"archived_at IS NULL"))
      .query[Restaurant]
      .to[List]
}

object RestaurantRepositoryDoobie {
  private val table             = fr"restaurant"
  private val fromTable         = fr"FROM" ++ table
  private val columns: Fragment = fr"id, email, name, created_by, created_at, archived_at"
}
