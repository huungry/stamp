package pl.hungry.main

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import pl.hungry.main.AppConfig.{DatabaseConfig, HttpConfig, JwtConfig}
import pureconfig.error.ConfigReaderException
import pureconfig.generic.semiauto.deriveReader
import pureconfig.{ConfigReader, ConfigSource}

import scala.concurrent.duration.FiniteDuration

final case class AppConfig(
  database: DatabaseConfig,
  http: HttpConfig,
  jwt: JwtConfig)

object AppConfig extends LazyLogging {

  final case class HttpConfig(host: String, port: Int)

  final case class DatabaseConfig(
    url: String,
    user: String,
    password: String,
    driver: String)

  final case class JwtConfig(secret: String, expiration: FiniteDuration)

  implicit val databaseConfigReader: ConfigReader[DatabaseConfig] = deriveReader
  implicit val httpConfigReader: ConfigReader[HttpConfig]         = deriveReader
  implicit val appConfigReader: ConfigReader[AppConfig]           = deriveReader
  implicit val jwtConfigReader: ConfigReader[JwtConfig]           = deriveReader

  def load(namespace: String): IO[AppConfig] =
    ConfigSource.default.at(namespace).load[AppConfig] match {
      case Right(config) => IO.pure(config)
      case Left(failures) =>
        logger.error(s"Failed to load config for namespace: $namespace. Failures: ${failures.prettyPrint()}")
        IO.raiseError(ConfigReaderException(failures))
    }

}
