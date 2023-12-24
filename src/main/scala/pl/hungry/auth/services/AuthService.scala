package pl.hungry.auth.services

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.syntax.EncoderOps
import pdi.jwt.{JwtAlgorithm, JwtCirce}
import pl.hungry.auth.domain.{AuthContext, JwtToken}
import pl.hungry.auth.repositories.AuthRepository
import pl.hungry.auth.services.AuthService.AuthError
import pl.hungry.auth.services.AuthService.AuthError._
import pl.hungry.main.AppConfig.{JwtConfig, jwtConfigReader}
import pl.hungry.user.domain.UserId
import pl.hungry.utils.error.DomainError

import scala.util.{Failure, Success}

class AuthService(
  authRepository: AuthRepository[ConnectionIO],
  transactor: Transactor[IO],
  config: JwtConfig) {
  import pl.hungry.user.protocols.UserCodecs._

  private val key                           = config.secret
  private val algo: JwtAlgorithm.HS256.type = JwtAlgorithm.HS256

  def encode(userId: UserId): ConnectionIO[Option[JwtToken]] =
    for {
      maybeUser <- authRepository.find(userId)
    } yield maybeUser.map(user => encodeToken(user.id))

  private def encodeToken(userId: UserId): JwtToken =
    JwtToken(JwtCirce.encode(userId.asJson, key, algo))

  def decode(jwtToken: JwtToken): IO[Either[AuthError, AuthContext]] =
    decodeJson(jwtToken.token) match {
      case Some(userId) => authRepository.find(userId).map(_.map(user => AuthContext(user.id)).toRight(UserNotFound())).transact(transactor)
      case None         => IO.pure(Left(InvalidToken()))
    }

  private def decodeJson(token: String): Option[UserId] =
    JwtCirce.decodeJson(token, key, Seq(algo)) match {
      case Success(json) => json.as[UserId].toOption
      case Failure(_)    => None
    }
}

object AuthService {
  sealed trait AuthError extends DomainError
  object AuthError {
    case class InvalidToken(message: String = "Invalid token")            extends AuthError
    case class UserNotFound(message: String = "User for token not found") extends AuthError
  }
}
