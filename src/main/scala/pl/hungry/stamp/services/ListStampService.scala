package pl.hungry.stamp.services

import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.stamp.domain.StampView
import pl.hungry.stamp.repositories.StampRepository

class ListStampService(stampRepository: StampRepository[ConnectionIO], transactor: Transactor[IO]) {
  def listView(authContext: AuthContext): IO[List[StampView]] = {
    val query = for {
      now        <- Clock[ConnectionIO].realTimeInstant
      stampsView <- stampRepository.listView(authContext.userId, now)
    } yield stampsView.sortWith { case (a, b) =>
      a.stampsToReward.map(_.value - a.count.value).getOrElse(1000) < b.stampsToReward.map(_.value - b.count.value).getOrElse(1000)
    }

    query.transact(transactor)
  }
}
