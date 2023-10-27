package pl.hungry.collection.domain.dto

import io.scalaland.chimney.dsl._
import pl.hungry.collection.domain.{CollectionId, ConfirmedCollection}
import pl.hungry.reward.domain.RewardId
import pl.hungry.stamp.domain.StampId
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class ConfirmedCollectionDto(
  id: CollectionId,
  userId: UserId,
  rewardId: RewardId,
  stampsIdUsed: List[StampId],
  createdAt: Instant,
  confirmedBy: UserId,
  confirmedAt: Instant)

object ConfirmedCollectionDto {
  def from(unconfirmedCollection: ConfirmedCollection): ConfirmedCollectionDto =
    unconfirmedCollection
      .into[ConfirmedCollectionDto]
      .withFieldConst(_.stampsIdUsed, unconfirmedCollection.stampsIdUsed.toList)
      .transform
}
