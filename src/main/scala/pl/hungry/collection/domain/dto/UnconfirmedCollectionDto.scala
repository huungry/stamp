package pl.hungry.collection.domain.dto

import io.scalaland.chimney.dsl._
import pl.hungry.collection.domain.{CollectionId, UnconfirmedCollection}
import pl.hungry.reward.domain.RewardId
import pl.hungry.stamp.domain.StampId
import pl.hungry.user.domain.UserId

import java.time.Instant

final case class UnconfirmedCollectionDto(
  id: CollectionId,
  userId: UserId,
  rewardId: RewardId,
  stampsIdUsed: List[StampId],
  createdAt: Instant)

object UnconfirmedCollectionDto {
  def from(unconfirmedCollection: UnconfirmedCollection): UnconfirmedCollectionDto =
    unconfirmedCollection
      .into[UnconfirmedCollectionDto]
      .withFieldConst(_.stampsIdUsed, unconfirmedCollection.stampsIdUsed.toList)
      .transform
}
