package pl.hungry.main

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import doobie.util.transactor.Transactor
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pl.hungry.main.AppConfig.{DatabaseConfig, HttpConfig}
import pl.hungry.utils.error.DecodeError
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.model.ValuedEndpointOutput

object Main extends IOApp.Simple {

  private val appName: String = "stamp"

  override def run: IO[Unit] = {
    val resource: Resource[IO, Server] = for {
      config        <- Resource.eval(AppConfig.load(appName))
      loggerFactory <- buildLoggerFactory
      transactor    <- buildTransactor(config.database)
      modules = AppBuilder.buildModules(transactor, config)(loggerFactory)
      routes  = AppBuilder.buildApp(modules)
      server <- serverResource(config.http, routes)
    } yield server

    resource.use(_ => IO.never)
  }

  private def buildTransactor(config: DatabaseConfig): Resource[IO, Transactor[IO]] =
    Resource.eval(IO(Transactor.fromDriverManager[IO](config.driver, config.url, config.user, config.password)))

  private def buildLoggerFactory: Resource[IO, LoggerFactory[IO]] =
    Resource.eval(IO(LoggerFactory[IO](Slf4jFactory.create[IO])))

  private def serverResource(httpConfig: HttpConfig, routes: List[ServerEndpoint[Any, IO]]): Resource[IO, Server] = {
    import pl.hungry.utils.error.DomainErrorCodecs._
    def encodeErrorResponse(m: String): ValuedEndpointOutput[_] =
      ValuedEndpointOutput(jsonBody[DecodeError], DecodeError(m))

    val options                    = Http4sServerOptions.customiseInterceptors[IO].defaultHandlers(encodeErrorResponse).options
    val httpRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO](options).toRoutes(routes)
    val HttpConfig(host, port)     = httpConfig

    for {
      host <- Resource.eval(IO.fromOption(Host.fromString(host))(new IllegalArgumentException(s"Invalid host [$host]")))
      port <- Resource.eval(IO.fromOption(Port.fromInt(port))(new IllegalArgumentException(s"Invalid port [$port]")))
      server <- EmberServerBuilder
                  .default[IO]
                  .withHost(host)
                  .withPort(port)
                  .withHttpApp(httpRoutes.orNotFound)
                  .build
    } yield server
  }
}
