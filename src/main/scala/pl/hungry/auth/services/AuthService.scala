package pl.hungry.auth.services

import cats.effect.IO
import cats.effect.kernel.Clock
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.syntax.EncoderOps
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import pl.hungry.auth.domain.{AuthContext, AuthUser, JwtContent, JwtToken}
import pl.hungry.auth.repositories.AuthRepository
import pl.hungry.auth.services.AuthService.AuthError._
import pl.hungry.auth.services.AuthService._
import pl.hungry.main.AppConfig.JwtConfig
import pl.hungry.utils.error.DomainError

import java.time.Instant
import scala.util.{Failure, Success}

class AuthService(
  authRepository: AuthRepository[ConnectionIO],
  transactor: Transactor[IO],
  config: JwtConfig
)(implicit loggerFactory: LoggerFactory[IO]) {
  import pl.hungry.auth.protocols.AuthCodecs._

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLogger
  private val key                                   = config.secret
  private val algo: JwtAlgorithm.HS256.type         = JwtAlgorithm.HS256

  def encode(user: AuthUser): IO[JwtToken] =
    Clock[IO].realTimeInstant.map(now => encodeToken(JwtContent(user.id), now))

  private def encodeToken(jwtContent: JwtContent, now: Instant): JwtToken = {
    val claim = JwtClaim(
      content = jwtContent.asJson.noSpaces,
      expiration = Some(now.plusSeconds(config.expiration.toSeconds).getEpochSecond),
      issuedAt = Some(now.getEpochSecond)
    )
    JwtToken(JwtCirce.encode(claim, key, algo))
  }

  def decode(jwtToken: JwtToken): IO[Either[AuthError, AuthContext]] =
    decodeToken(jwtToken.token).flatMap {
      case Some(jwtContent) =>
        authRepository
          .find(jwtContent.userId)
          .map(_.map(user => AuthContext(user.id)).toRight(UserNotFound()))
          .transact(transactor)
      case None => IO.pure(Left(InvalidToken()))
    }

  private def decodeToken(token: String): IO[Option[JwtContent]] =
    IO(JwtCirce.decode(token, key, Seq(algo))).flatMap {
      case Success(jwtClaim) => IO(io.circe.parser.decode[JwtContent](jwtClaim.content).toOption)
      case Failure(e)        => logger.warn(e)(s"Failed to decode token: $token").as(None)
    }
}

object AuthService {
  sealed trait AuthError extends DomainError
  object AuthError {
    case class InvalidToken(message: String = "Invalid token")            extends AuthError
    case class UserNotFound(message: String = "User for token not found") extends AuthError
  }
}
