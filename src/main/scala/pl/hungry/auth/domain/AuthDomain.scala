package pl.hungry.auth.domain

import eu.timepit.refined.types.string.NonEmptyString

import java.security.SecureRandom
import scala.util.Random

final case class RefreshToken(token: NonEmptyString)
object RefreshToken {
  private val secureRandom = new SecureRandom()
  def generate: RefreshToken =
    RefreshToken(NonEmptyString.unsafeFrom(new Random(secureRandom.nextLong()).alphanumeric.take(32).mkString))
}

final case class UserAgent(value: NonEmptyString)
object UserAgent {
  def tryFrom(value: String): Option[UserAgent] = NonEmptyString.from(value).map(UserAgent(_)).toOption
}
