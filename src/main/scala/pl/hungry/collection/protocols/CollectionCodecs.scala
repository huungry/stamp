package pl.hungry.collection.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.collection.domain._

object CollectionCodecs {
  import pl.hungry.reward.protocols.RewardCodecs._
  import pl.hungry.stamp.protocols.StampCodecs._
  import pl.hungry.user.protocols.UserCodecs._

  implicit val collectionIdCodec: Codec[CollectionId]                   = deriveUnwrappedCodec
  implicit val unconfirmedCollectionCodec: Codec[UnconfirmedCollection] = deriveCodec
  implicit val confirmedCollectionCodec: Codec[ConfirmedCollection]     = deriveCodec
}
