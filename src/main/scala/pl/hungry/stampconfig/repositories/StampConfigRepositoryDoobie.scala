package pl.hungry.stampconfig.repositories

import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import doobie.util.{Read, Write}
import doobie.{ConnectionIO, Fragments}
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.reward.domain.RewardId
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.repositories.StampConfigRepositoryDoobie._

import java.time.Instant
import java.util.UUID

final class StampConfigRepositoryDoobie extends StampConfigRepository[ConnectionIO] {
  override def findCurrent(restaurantId: RestaurantId, now: Instant): ConnectionIO[Option[StampConfig]] =
    (fr"SELECT" ++ columns ++ fromTable ++ Fragments.whereAnd(
      fr"restaurant_id = ${restaurantId.value}",
      fr"created_at <= $now"
    ) ++ fr"ORDER BY created_at DESC")
      .query[StampConfig]
      .option

  override def insert(stampConfig: StampConfig): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${stampConfig.id.value},
         ${stampConfig.restaurantId.value},
         ${stampConfig.stampsToReward.value},
         ${stampConfig.rewards.map(_.value)},
         ${stampConfig.createdAt},
         NULL)""".update.run
}

object StampConfigRepositoryDoobie {
  private val table     = fr"stamp_config"
  private val fromTable = fr"FROM" ++ table
  private val columns   = fr"id, restaurant_id, stamps_to_reward, rewards, created_at, archived_at"

  implicit val rewardIdListRead: Read[List[RewardId]]   = Read[List[UUID]].map(_.map(uuid => RewardId(uuid)))
  implicit val rewardIdListWrite: Write[List[RewardId]] = Write[List[UUID]].contramap(_.map(_.value))
}
