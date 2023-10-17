package pl.hungry.stampconfig.services

import cats.data.EitherT
import cats.effect.{Clock, IO}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pl.hungry.restaurant.domain.RestaurantId
import pl.hungry.stampconfig.domain.StampConfig
import pl.hungry.stampconfig.repositories.StampConfigRepository
import pl.hungry.stampconfig.services.FindStampConfigService.FindStampConfigError
import pl.hungry.utils.error.DomainError

import java.time.Instant

class FindStampConfigService(stampConfigRepository: StampConfigRepository[ConnectionIO], transactor: Transactor[IO]) {
  def find(restaurantId: RestaurantId): IO[Either[FindStampConfigError, StampConfig]] = {
    val effect = for {
      // TODO ensure active restaurant exists
      now         <- getTime
      stampConfig <- findStampConfig(restaurantId, now)
    } yield stampConfig

    effect.value.transact(transactor)
  }

  private def getTime: EitherT[ConnectionIO, FindStampConfigError, Instant] =
    EitherT.right(Clock[ConnectionIO].realTimeInstant)

  private def findStampConfig(restaurantId: RestaurantId, now: Instant): EitherT[ConnectionIO, FindStampConfigError, StampConfig] =
    EitherT.fromOptionF(stampConfigRepository.findCurrent(restaurantId, now), FindStampConfigError.StampConfigNotFound())
}

object FindStampConfigService {
  sealed trait FindStampConfigError extends DomainError
  object FindStampConfigError {
    case class StampConfigNotFound(message: String = "Stamp config not found") extends FindStampConfigError
  }
}
