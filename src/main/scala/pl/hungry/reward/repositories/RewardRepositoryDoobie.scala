package pl.hungry.reward.repositories

import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import doobie.{ConnectionIO, Fragments}
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.{Reward, RewardId, RewardName}
import pl.hungry.reward.repositories.RewardRepositoryDoobie._

final class RewardRepositoryDoobie extends RewardRepository[ConnectionIO] {
  override def findActive(id: RewardId): ConnectionIO[Option[Reward]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(fr"id = ${id.value}", fr"archived_at IS NULL"))
      .query[Reward]
      .option

  override def findActive(name: RewardName, restaurantId: RestaurantId): ConnectionIO[Option[Reward]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(
      fr"restaurant_id = ${restaurantId.value}",
      fr"name = ${name.value.value}",
      fr"archived_at IS NULL"
    ))
      .query[Reward]
      .option

  override def listActive(restaurantId: RestaurantId): ConnectionIO[List[Reward]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(fr"restaurant_id = ${restaurantId.value}", fr"archived_at IS NULL"))
      .query[Reward]
      .to[List]

  override def insert(reward: Reward): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${reward.id.value},
         ${reward.name.value.value},
         ${reward.restaurantId.value},
         NULL)""".update.run

}

object RewardRepositoryDoobie {
  private val table     = fr"reward"
  private val fromTable = fr"FROM" ++ table
  private val columns   = fr"id, name, restaurant_id, archived_at"
}
