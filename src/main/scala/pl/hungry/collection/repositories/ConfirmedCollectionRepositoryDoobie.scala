package pl.hungry.collection.repositories

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import pl.hungry.collection.domain.ConfirmedCollection
import pl.hungry.collection.repositories.ConfirmedCollectionRepositoryDoobie._

final class ConfirmedCollectionRepositoryDoobie extends ConfirmedCollectionRepository[ConnectionIO] {

  override def insert(confirmedCollection: ConfirmedCollection): ConnectionIO[Int] =
    sql"""INSERT INTO $table VALUES
         (${confirmedCollection.id.value},
         ${confirmedCollection.userId.value},
         ${confirmedCollection.rewardId.value},
         ${confirmedCollection.stampsIdUsed.toList.map(_.value)},
          ${confirmedCollection.createdAt},
          ${confirmedCollection.confirmedBy.value},
          ${confirmedCollection.confirmedAt})""".update.run
}

object ConfirmedCollectionRepositoryDoobie {
  private val table = fr"confirmed_collection"
}
