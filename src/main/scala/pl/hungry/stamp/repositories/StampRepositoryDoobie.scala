package pl.hungry.stamp.repositories

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import doobie.{ConnectionIO, Fragments}
import eu.timepit.refined.types.numeric.PosInt
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stamp.domain.{Stamp, StampId, StampView}
import pl.hungry.stamp.repositories.StampRepositoryDoobie._
import pl.hungry.user.domain.UserId

import java.time.Instant

final class StampRepositoryDoobie extends StampRepository[ConnectionIO] {
  override def insert(stamp: Stamp): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${stamp.id.value},
         ${stamp.restaurantId.value},
         ${stamp.userId.value},
         ${stamp.createdAt},
         NULL)""".update.run

  override def listView(userId: UserId, now: Instant): ConnectionIO[List[StampView]] =
    sql"""
    SELECT DISTINCT ON (r.id)
        r.id,
        r.name,
        sc.stamps_to_reward,
        COUNT(*)
    FROM
        stamp s
    JOIN
        restaurant r ON s.restaurant_id = r.id
    LEFT JOIN
        stamp_config sc ON sc.restaurant_id = r.id
    WHERE
        user_id = ${userId.value}
        AND used_at IS NULL
        AND (sc.created_at <= $now OR sc.created_at IS NULL)
    GROUP BY
        r.id, r.name, sc.stamps_to_reward, sc.created_at
    ORDER BY
        r.id, sc.created_at DESC
         """.stripMargin
      .query[StampView]
      .to[List]

  override def listActiveForUpdate(
    userId: UserId,
    restaurantId: RestaurantId,
    limit: PosInt
  ): ConnectionIO[List[StampId]] =
    (fr"SELECT" ++ id ++ fromTable ++ Fragments.whereAnd(
      fr"user_id = ${userId.value}",
      fr"restaurant_id = ${restaurantId.value}",
      fr"used_at IS NULL"
    ) ++ fr"LIMIT ${limit.value} FOR UPDATE")
      .query[StampId]
      .to[List]

  override def markAsUsed(stampsId: NonEmptyList[StampId], now: Instant): ConnectionIO[Int] =
    (fr"UPDATE" ++ table ++ fr"SET used_at = $now" ++ Fragments.whereAnd(Fragments.in(fr"id", stampsId.map(_.value)))).update.run
}

object StampRepositoryDoobie {
  private val table     = fr"stamp"
  private val fromTable = fr"FROM" ++ table
  private val id        = fr"id"
}
