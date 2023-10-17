import _root_.io.github.davidmweber.FlywayPlugin.autoImport.*
import sbt.*
import sbt.Keys.*

object PostgresMigrations {

  private def commonSettings =
    Seq(flywayLocations := Seq(s"filesystem:${(Compile / resourceDirectory).value.getPath}/db/migration"))

  def settings: Seq[Setting[?]] = {
    val url      = sys.env.get("DB_URL").map(url => flywayUrl := url).getOrElse(flywayUrl := "jdbc:postgresql://localhost:5432/stamp")
    val user     = sys.env.get("DB_USER").map(user => flywayUser := user).getOrElse(flywayUser := "postgres")
    val password = sys.env.get("DB_PASSWORD").map(password => flywayPassword := password).getOrElse(flywayPassword := "hardpssword")

    commonSettings ++ Seq(url, user, password)
  }
}
