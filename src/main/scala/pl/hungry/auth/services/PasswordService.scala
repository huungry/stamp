package pl.hungry.auth.services

import eu.timepit.refined.types.string.NonEmptyString
import org.mindrot.jbcrypt.BCrypt
import pl.hungry.user.domain.{PasswordHash, PasswordPlain}

class PasswordService {
  def hash(passwordPlain: PasswordPlain): PasswordHash =
    PasswordHash(NonEmptyString.unsafeFrom(BCrypt.hashpw(passwordPlain.value.value, BCrypt.gensalt(12))))

  def isValid(candidate: PasswordPlain, hashed: PasswordHash): Boolean =
    BCrypt.checkpw(candidate.value.value, hashed.value.value)
}
