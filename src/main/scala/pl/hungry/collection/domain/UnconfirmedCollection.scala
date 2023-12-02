package pl.hungry.collection.domain

import pl.hungry.reward.domain.RewardId
import pl.hungry.stamp.domain.StampId
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class UnconfirmedCollection(
  id: CollectionId,
  userId: UserId,
  rewardId: RewardId,
  stampsIdUsed: List[StampId],
  createdAt: Instant)

object UnconfirmedCollection {
  def from(
    userId: UserId,
    rewardId: RewardId,
    stampsId: List[StampId],
    now: Instant
  ): UnconfirmedCollection =
    UnconfirmedCollection(id = CollectionId.generate, userId = userId, rewardId = rewardId, stampsIdUsed = stampsId, createdAt = now)
}
