package pl.hungry.collection.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import doobie.util.{Read, Write}
import pl.hungry.collection.domain.{CollectionId, UnconfirmedCollection}
import pl.hungry.collection.repositories.UnconfirmedCollectionRepositoryDoobie._
import pl.hungry.stamp.domain.StampId

import java.util.UUID

final class UnconfirmedCollectionRepositoryDoobie extends UnconfirmedCollectionRepository[ConnectionIO] {

  override def delete(id: CollectionId): ConnectionIO[Int] =
    (fr"DELETE" ++ fromTable ++ fr"WHERE id = ${id.value}").update.run

  override def find(id: CollectionId): ConnectionIO[Option[UnconfirmedCollection]] =
    (fr"SELECT" ++ columns ++ fromTable ++ fr"WHERE id = ${id.value}")
      .query[UnconfirmedCollection]
      .option

  override def insert(unconfirmedCollection: UnconfirmedCollection): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${unconfirmedCollection.id.value},
         ${unconfirmedCollection.userId.value},
         ${unconfirmedCollection.rewardId.value},
         ${unconfirmedCollection.stampsIdUsed.toList.map(_.value)},
          ${unconfirmedCollection.createdAt})""".update.run
}

object UnconfirmedCollectionRepositoryDoobie {
  private val table     = fr"unconfirmed_collection"
  private val columns   = fr"id, user_id, reward_id, stamps_id_used, created_at"
  private val fromTable = fr"FROM" ++ table

  implicit val stampIdListRead: Read[List[StampId]]   = Read[List[UUID]].map(_.map(uuid => StampId(uuid)))
  implicit val stampIdListWrite: Write[List[StampId]] = Write[List[UUID]].contramap(_.map(_.value))
}
