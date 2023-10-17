package pl.hungry.restaurant.domain

import enumeratum._
import eu.timepit.refined.types.string.NonEmptyString
import pl.hungry.utils.refinements.Refinements.Email

import java.util.UUID

final case class RestaurantId(value: UUID) extends AnyVal
object RestaurantId {
  def generate: RestaurantId = RestaurantId(UUID.randomUUID())
}
final case class RestaurantName(value: NonEmptyString)
final case class RestaurantEmail(value: Email)

final case class RestaurantUserId(value: UUID) extends AnyVal
object RestaurantUserId {
  def generate: RestaurantUserId = RestaurantUserId(UUID.randomUUID())
}

sealed trait Position extends EnumEntry
object Position extends Enum[Position] with CirceEnum[Position] with DoobieEnum[Position] {
  case object Employee extends Position
  case object Manager  extends Position
  override def values: IndexedSeq[Position] = findValues
}
