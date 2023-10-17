package pl.hungry

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.StrictLogging
import org.postgresql.jdbc.PgConnection
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest._
import pl.hungry.main.AppConfig.DatabaseConfig

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
  private var postgres: EmbeddedPostgres        = _
  private var currentDbConfig: DatabaseConfig   = _
  var url: String                               = _
  var currentTestWithDatabase: TestWithDatabase = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    postgres = EmbeddedPostgres.builder().start()
    url = postgres.getJdbcUrl("postgres")
    postgres.getPostgresDatabase.getConnection.asInstanceOf[PgConnection].setPrepareThreshold(100)
    currentDbConfig = DatabaseConfig(url = url, user = "postgres", password = "", driver = "org.postgresql.Driver")
    currentTestWithDatabase = new TestWithDatabase(currentDbConfig)
    currentTestWithDatabase.testConnection()
  }

  override protected def afterAll(): Unit = {
    postgres.close()
    currentTestWithDatabase.close()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    currentTestWithDatabase.migrate()
  }

  override protected def afterEach(): Unit = {
    currentTestWithDatabase.clean()
    super.afterEach()
  }

  lazy val db        = new DatabaseAccess(currentTestWithDatabase.xa)
  lazy val endpoints = new Endpoints(currentTestWithDatabase.backendStub, db: DatabaseAccess)
}
