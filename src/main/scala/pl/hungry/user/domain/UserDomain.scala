package pl.hungry.user.domain

import enumeratum._
import eu.timepit.refined.types.string.NonEmptyString
import pl.hungry.utils.refinements.Refinements.Email

import java.util.UUID

final case class UserId(value: UUID) extends AnyVal
object UserId {
  def generate: UserId = UserId(UUID.randomUUID())
}
final case class UserEmail(value: Email)
final case class PasswordPlain(value: NonEmptyString)
final case class PasswordHash(value: NonEmptyString)
final case class FirstName(value: NonEmptyString)
final case class LastName(value: NonEmptyString)
final case class NickName(value: NonEmptyString)

sealed trait UserRole extends EnumEntry
object UserRole extends Enum[UserRole] with CirceEnum[UserRole] with DoobieEnum[UserRole] {
  case object Basic extends UserRole
  case object Pro   extends UserRole

  override def values: IndexedSeq[UserRole] = findValues
}
