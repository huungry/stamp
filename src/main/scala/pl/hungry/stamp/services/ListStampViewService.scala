package pl.hungry.stamp.services

import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.auth.domain.AuthContext
import pl.hungry.stamp.domain.StampView
import pl.hungry.stamp.repositories.StampRepository

class ListStampViewService(stampRepository: StampRepository[ConnectionIO], transactor: Transactor[IO]) {
  def listView(authContext: AuthContext): IO[List[StampView]] = {
    val query = for {
      now        <- Clock[ConnectionIO].realTimeInstant
      stampsView <- stampRepository.listView(authContext.userId, now)
    } yield stampsView.sortWith { case (a, b) =>
      val stillNeededForRewardA = a.stampsToReward.fold(Int.MaxValue)(_.value - a.collectedStamps.value)
      val stillNeededForRewardB = b.stampsToReward.fold(Int.MaxValue)(_.value - b.collectedStamps.value)

      stillNeededForRewardA < stillNeededForRewardB
    }

    query.transact(transactor)
  }
}
