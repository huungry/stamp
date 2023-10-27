val tapirVersion  = "1.2.9"
val doobieVersion = "1.0.0-RC2"

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

lazy val stamp = (project in file(".")).settings(
  Seq(
    name := "stamp",
    version := "0.1.0-SNAPSHOT",
    organization := "pl.hungry",
    scalaVersion := "2.13.10",
    scalacOptions := sbtOptions,
    IntegrationTest / scalacOptions += "-Wconf:msg=unused value of type org.scalatest.Assertion.*:s",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"     % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"  % tapirVersion % "test,it",
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"        % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-refined"           % tapirVersion,
      "org.tpolecat"                  %% "doobie-core"             % doobieVersion,
      "org.tpolecat"                  %% "doobie-postgres"         % doobieVersion,
      "org.tpolecat"                  %% "doobie-refined"          % doobieVersion,
      "org.tpolecat"                  %% "doobie-hikari"           % doobieVersion,
      "org.flywaydb"                   % "flyway-core"             % "9.15.1",
      "org.http4s"                    %% "http4s-ember-server"     % "0.23.18",
      "io.circe"                      %% "circe-refined"           % "0.14.3",
      "io.circe"                      %% "circe-generic-extras"    % "0.14.3",
      "ch.qos.logback"                 % "logback-classic"         % "1.4.6",
      "org.scalatest"                 %% "scalatest"               % "3.2.15"     % "test,it",
      "com.softwaremill.sttp.client3" %% "circe"                   % "3.8.13"     % "test,it",
      "eu.timepit"                    %% "refined"                 % "0.10.3",
      "eu.timepit"                    %% "refined-eval"            % "0.10.3",
      "eu.timepit"                    %% "refined-pureconfig"      % "0.10.3",
      "eu.timepit"                    %% "refined-scalacheck"      % "0.10.3",
      "com.github.pureconfig"         %% "pureconfig"              % "0.17.2",
      "com.beachape"                  %% "enumeratum"              % "1.7.2",
      "com.beachape"                  %% "enumeratum-circe"        % "1.7.2",
      "com.beachape"                  %% "enumeratum-doobie"       % "1.7.3",
      "com.typesafe.scala-logging"    %% "scala-logging"           % "3.9.5",
      "com.github.pureconfig"         %% "pureconfig"              % "0.17.2",
      "com.github.jwt-scala"          %% "jwt-circe"               % "9.2.0",
      "org.http4s"                    %% "http4s-circe"            % "0.23.18",
      "org.http4s"                    %% "http4s-dsl"              % "0.23.18",
      "io.scalaland"                  %% "chimney"                 % "0.7.2",
      "org.mindrot"                    % "jbcrypt"                 % "0.4",
      "com.opentable.components"       % "otj-pg-embedded"         % "1.0.1"      % "test,it",
      "com.typesafe.scala-logging"    %% "scala-logging"           % "3.9.5",
      "ch.qos.logback"                 % "logback-classic"         % "1.4.6",
      "org.codehaus.janino"            % "janino"                  % "3.1.9"      % Runtime,
      "com.softwaremill.macwire"      %% "macros"                  % "2.5.8"      % "provided",
      "com.softwaremill.macwire"      %% "util"                    % "2.5.8",
      "com.softwaremill.macwire"      %% "proxy"                   % "2.5.8",
      "org.scalacheck"                %% "scalacheck"              % "1.17.0"     % "test,it"
    )
  )
)

configs(IntegrationTest)
Seq(Defaults.itSettings: _*)

enablePlugins(FlywayPlugin)
PostgresMigrations.settings
