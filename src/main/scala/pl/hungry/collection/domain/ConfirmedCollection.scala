package pl.hungry.collection.domain

import io.scalaland.chimney.dsl._
import pl.hungry.reward.domain.RewardId
import pl.hungry.stamp.domain.StampId
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class ConfirmedCollection(
  id: CollectionId,
  userId: UserId,
  rewardId: RewardId,
  stampsIdUsed: List[StampId],
  createdAt: Instant,
  confirmedBy: UserId,
  confirmedAt: Instant)

object ConfirmedCollection {
  def from(
    unconfirmedCollection: UnconfirmedCollection,
    confirmedBy: UserId,
    now: Instant
  ): ConfirmedCollection =
    unconfirmedCollection
      .into[ConfirmedCollection]
      .withFieldConst(_.id, CollectionId.generate)
      .withFieldConst(_.confirmedBy, confirmedBy)
      .withFieldConst(_.confirmedAt, now)
      .transform
}
