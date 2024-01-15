package pl.hungry.auth.services

import cats.effect.IO
import cats.effect.kernel.Clock
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import pl.hungry.auth.domain._
import pl.hungry.auth.repositories.UserRefreshTokenRepository
import pl.hungry.user.domain._

class RefreshTokenService(
  userRefreshTokenRepository: UserRefreshTokenRepository[ConnectionIO],
  transactor: Transactor[IO]
)(implicit loggerFactory: LoggerFactory[IO]) {
  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLogger

  def upsert(userId: UserId, userAgentOpt: Option[UserAgent]): IO[Option[RefreshToken]] =
    userAgentOpt match {
      case None => logger.warn(s"User-Agent header is empty - cannot insert refresh token for user $userId").as(Option.empty[RefreshToken])
      case Some(userAgent) =>
        Clock[IO].realTimeInstant.flatMap { now =>
          val refreshToken     = RefreshToken.generate
          val userRefreshToken = UserRefreshToken.from(userId, refreshToken, userAgent, now)
          userRefreshTokenRepository.upsert(userRefreshToken).transact(transactor).map(_ => Some(refreshToken))
        }
    }
}
