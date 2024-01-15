package pl.hungry.auth.domain

import eu.timepit.refined.types.string.NonEmptyString
import pl.hungry.user.domain.UserId

import java.security.SecureRandom
import scala.util.Random

final case class JwtToken(token: String) extends AnyVal

final case class RefreshToken(value: NonEmptyString)
object RefreshToken {
  private val secureRandom = new SecureRandom()
  def generate: RefreshToken =
    RefreshToken(NonEmptyString.unsafeFrom(new Random(secureRandom.nextLong()).alphanumeric.take(64).mkString))
}

final case class UserAgent(value: NonEmptyString)
object UserAgent {
  def tryFrom(value: String): Option[UserAgent] = NonEmptyString.from(value).map(UserAgent(_)).toOption
}

final private[auth] case class JwtContent(userId: UserId)
