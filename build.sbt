val sbtOptions = Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:higherKinds",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Woctal-literal",
  "-Wunused:explicits",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:linted",
  "-Wunused:locals",
  "-Wunused:privates",
  "-Wvalue-discard",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:implicit-not-found",
  "-Xlint:implicit-recursion",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nonlocal-return",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:serial",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Xlint:valpattern",
  "-Wnonunit-statement"
)

val tapirVersion        = "1.4.0"
val doobieVersion       = "1.0.0-RC2"
val flywayVersion       = "9.16.0"
val http4sVersion       = "0.23.18"
val circeVersion        = "0.14.3"
val logbackVersion      = "1.4.7"
val scalatestVersion    = "3.2.15"
val sttpClientVersion   = "3.8.13"
val refinedVersion      = "0.10.3"
val pureconfigVersion   = "0.17.4"
val enumeratumVersion   = "1.7.2"
val scalaLoggingVersion = "3.9.5"
val jwtScalaVersion     = "9.2.0"
val chimneyVersion      = "0.7.2"
val jbcryptVersion      = "0.4"
val embeddedPgVersion   = "1.0.1"
val janinoVersion       = "3.1.9"
val scalacheckVersion   = "1.17.0"

lazy val stamp = (project in file(".")).settings(
  name := "stamp",
  version := "latest",
  organization := "pl.hungry",
  scalaVersion := "2.13.10",
  scalacOptions := sbtOptions,
  IntegrationTest / scalacOptions += "-Wconf:msg=unused value of type org.scalatest.Assertion.*:s",
  libraryDependencies ++= Seq(
    "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"     % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"  % tapirVersion      % "test,it",
    "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"        % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-refined"           % tapirVersion,
    "org.tpolecat"                  %% "doobie-core"             % doobieVersion,
    "org.tpolecat"                  %% "doobie-postgres"         % doobieVersion,
    "org.tpolecat"                  %% "doobie-refined"          % doobieVersion,
    "org.tpolecat"                  %% "doobie-hikari"           % doobieVersion,
    "org.flywaydb"                   % "flyway-core"             % flywayVersion,
    "org.http4s"                    %% "http4s-ember-server"     % http4sVersion,
    "io.circe"                      %% "circe-refined"           % circeVersion,
    "io.circe"                      %% "circe-generic-extras"    % circeVersion,
    "ch.qos.logback"                 % "logback-classic"         % logbackVersion,
    "org.scalatest"                 %% "scalatest"               % scalatestVersion  % "test,it",
    "com.softwaremill.sttp.client3" %% "circe"                   % sttpClientVersion % "test,it",
    "eu.timepit"                    %% "refined"                 % refinedVersion,
    "eu.timepit"                    %% "refined-eval"            % refinedVersion,
    "eu.timepit"                    %% "refined-pureconfig"      % refinedVersion,
    "eu.timepit"                    %% "refined-scalacheck"      % refinedVersion,
    "com.github.pureconfig"         %% "pureconfig"              % pureconfigVersion,
    "com.beachape"                  %% "enumeratum"              % enumeratumVersion,
    "com.beachape"                  %% "enumeratum-circe"        % enumeratumVersion,
    "com.beachape"                  %% "enumeratum-doobie"       % enumeratumVersion,
    "com.typesafe.scala-logging"    %% "scala-logging"           % scalaLoggingVersion,
    "com.github.jwt-scala"          %% "jwt-circe"               % jwtScalaVersion,
    "org.http4s"                    %% "http4s-circe"            % http4sVersion,
    "org.http4s"                    %% "http4s-dsl"              % http4sVersion,
    "io.scalaland"                  %% "chimney"                 % chimneyVersion,
    "org.mindrot"                    % "jbcrypt"                 % jbcryptVersion,
    "com.opentable.components"       % "otj-pg-embedded"         % embeddedPgVersion % "test,it",
    "ch.qos.logback"                 % "logback-classic"         % logbackVersion,
    "org.codehaus.janino"            % "janino"                  % janinoVersion     % Runtime,
    "org.scalacheck"                %% "scalacheck"              % scalacheckVersion % "test,it"
  )
)

configs(IntegrationTest)
Seq(Defaults.itSettings: _*)

enablePlugins(FlywayPlugin)
PostgresMigrations.settings

enablePlugins(JavaAppPackaging, DockerPlugin)

Docker / packageName := "huungry/stamp"
dockerBaseImage := "azul/zulu-openjdk:17.0.9"
