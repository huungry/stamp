package pl.hungry.collection.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.collection.domain._
import pl.hungry.collection.domain.dto.{ConfirmedCollectionDto, UnconfirmedCollectionDto}

object CollectionCodecs {
  import pl.hungry.reward.protocols.RewardCodecs._
  import pl.hungry.stamp.protocols.StampCodecs._
  import pl.hungry.user.protocols.UserCodecs._

  implicit val collectionIdCodec: Codec[CollectionId]                         = deriveUnwrappedCodec
  implicit val unconfirmedCollectionDtoCodec: Codec[UnconfirmedCollectionDto] = deriveCodec
  implicit val confirmedCollectionDtoCodec: Codec[ConfirmedCollectionDto]     = deriveCodec
}
