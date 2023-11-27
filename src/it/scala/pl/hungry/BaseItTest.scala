package pl.hungry

import cats.effect.IO
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.StrictLogging
import org.postgresql.jdbc.PgConnection
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pl.hungry.auth.AuthModule
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.collection.CollectionModule
import pl.hungry.main.AppBuilder
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.main.AppConfig.DatabaseConfig
import pl.hungry.restaurant.{DatabaseAccessRestaurant, DatabaseAccessRestaurantFactory, RestaurantModule}
import pl.hungry.reward.RewardModule
import pl.hungry.stamp.StampModule
import pl.hungry.stampconfig.StampConfigModule
import pl.hungry.user.UserModule
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.integ.cats.effect.CatsMonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter

/** Based on https://github.com/softwaremill/bootzooka */
trait BaseItTest
    extends AnyFlatSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with Matchers
    with EitherValues
    with OptionValues
    with StrictLogging
    with TestSupport {
  self: Suite =>
  private var postgres: EmbeddedPostgres      = _
  private var currentDbConfig: DatabaseConfig = _
  var url: String                             = _
  var currentTest: TestWithDatabase           = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    postgres = EmbeddedPostgres.builder().start()
    url = postgres.getJdbcUrl("postgres")
    postgres.getPostgresDatabase.getConnection.asInstanceOf[PgConnection].setPrepareThreshold(100)
    currentDbConfig = DatabaseConfig(url = url, user = "postgres", password = "", driver = "org.postgresql.Driver")
    currentTest = new TestWithDatabase(currentDbConfig)
    currentTest.testConnection()
  }

  override protected def afterAll(): Unit = {
    postgres.close()
    currentTest.close()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    currentTest.migrate()
  }

  override protected def afterEach(): Unit = {
    currentTest.clean()
    super.afterEach()
  }

  lazy val defaultTestAppModules: AppModules = {
    val authModule                     = AuthModule.make(currentTest.xa)
    val bearerEndpoint: BearerEndpoint = authModule.routes.bearerEndpoint

    val userModule       = UserModule.make(currentTest.xa, authModule.passwordService, bearerEndpoint)
    val restaurantModule = RestaurantModule.make(currentTest.xa, bearerEndpoint, userModule.userInternalService)
    val rewardModule     = RewardModule.make(currentTest.xa, bearerEndpoint, restaurantModule.restaurantInternalService)
    val stampModule = StampModule.make(currentTest.xa, bearerEndpoint, userModule.userInternalService, restaurantModule.restaurantInternalService)
    val stampConfigModule =
      StampConfigModule.make(currentTest.xa, bearerEndpoint, restaurantModule.restaurantInternalService, rewardModule.rewardInternalService)

    val collectionModule = CollectionModule.make(
      currentTest.xa,
      bearerEndpoint,
      restaurantModule.restaurantInternalService,
      rewardModule.rewardInternalService,
      stampConfigModule.stampConfigInternalService,
      stampModule.stampInternalService
    )

    AppModules(authModule, userModule, restaurantModule, rewardModule, stampModule, stampConfigModule, collectionModule)
  }

  def buildTestCaseSetup[DbAccess <: DatabaseAccess](appModules: AppModules, dbAccessFactory: DatabaseAccessFactory): (DbAccess, Endpoints) = {
    val app: List[ServerEndpoint[Any, IO]] = AppBuilder.buildApp(appModules)

    val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
      .whenServerEndpointsRunLogic(app)
      .backend()

    val db = dbAccessFactory.create(currentTest.xa).asInstanceOf[DbAccess]
    val dbAccessRestaurant = // TODO remove after payment service... endpoints should not depend on db access
      new DatabaseAccessRestaurantFactory()
        .create(currentTest.xa)
        .asInstanceOf[DatabaseAccessRestaurant]
    val endpoints = new Endpoints(backendStub, dbAccessRestaurant)

    (db, endpoints)
  }
}
