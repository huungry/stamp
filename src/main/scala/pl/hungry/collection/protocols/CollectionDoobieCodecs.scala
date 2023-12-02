package pl.hungry.collection.protocols

import doobie.postgres.implicits._
import doobie.util.{Read, Write}
import pl.hungry.stamp.domain.StampId

import java.util.UUID

trait CollectionDoobieCodecs {
  implicit val stampIdListRead: Read[List[StampId]]   = Read[List[UUID]].map(_.map(uuid => StampId(uuid)))
  implicit val stampIdListWrite: Write[List[StampId]] = Write[List[UUID]].contramap(_.map(_.value))
}
