package pl.hungry

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import pl.hungry.main.AppBuilder
import pl.hungry.main.AppConfig.DatabaseConfig
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.integ.cats.CatsMonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.annotation.tailrec
import scala.concurrent.duration._

/** Based on https://github.com/softwaremill/bootzooka */
class TestWithDatabase(config: DatabaseConfig) extends StrictLogging {

  var xa: Transactor[IO]                         = _
  private val xaReady: Queue[IO, Transactor[IO]] = Queue.unbounded[IO, Transactor[IO]].unsafeRunSync()
  private val done: Queue[IO, Unit]              = Queue.unbounded[IO, Unit].unsafeRunSync()

  {
    val xaResource = for {
      connectEC <- doobie.util.ExecutionContexts.fixedThreadPool[IO](8)
      xa        <- HikariTransactor.newHikariTransactor[IO](config.driver, config.url, config.user, config.password, connectEC)
    } yield xa

    xaResource
      .use { _xa =>
        xaReady.offer(_xa) >> done.take
      }
      .start
      .unsafeRunSync(): Unit

    xa = xaReady.take.unsafeRunSync()
  }

  lazy val app: List[ServerEndpoint[Any, IO]] = AppBuilder.buildModules(xa)

  lazy val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
    .whenServerEndpointsRunLogic(app)
    .backend()

  private val flyway =
    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .cleanDisabled(false)
      .load()

  @tailrec
  final def connectAndMigrate(): Unit =
    try {
      migrate()
      testConnection()
      logger.info("Database migration & connection test complete")
    } catch {
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Thread.sleep(5000)
        connectAndMigrate()
    }

  def migrate(): Unit =
    if (true) {
      flyway.migrate()
      ()
    }

  def clean(): Unit =
    flyway.clean(): Unit

  def testConnection(): Unit = {
    sql"select 1".query[Int].unique.transact(xa).unsafeRunTimed(1.minute): Unit
    ()
  }

  def close(): Unit =
    done.offer(()).unsafeRunTimed(1.minute): Unit
}
