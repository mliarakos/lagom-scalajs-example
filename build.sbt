import com.lightbend.lagom.core.LagomVersion
import sbt.Keys.{scalaVersion, version}
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

// Disable Cassandra and Kafka for performance since they are not used
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

val scalaVersions = Seq("2.12.10", "2.13.1")

lazy val scalaSettings = Seq(
  crossScalaVersions := scalaVersions,
  scalaVersion := scalaVersions.head,
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlog-reflective-calls"
  )
)

lazy val commonSettings = scalaSettings ++ Seq(
  organization := "com.github.mliarakos.lagom-scalajs-example",
  version := "0.5.0"
)

lazy val commonJsSettings = commonSettings

val lagomjsVersion = s"0.5.0-${LagomVersion.current}"
val macwire        = "com.softwaremill.macwire" %% "macros" % "2.3.7" % Provided

lazy val `example-api` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("example-api"))
  .settings(
    name := "example-api"
  )
  .jvmSettings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies ++= Seq(lagomScaladslApi)
  )
  .jsSettings(commonJsSettings: _*)
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.github.mliarakos.lagomjs" %%% "lagomjs-scaladsl-api" % lagomjsVersion
    )
  )

lazy val `example-impl` = project
  .in(file("example-impl"))
  .settings(commonSettings: _*)
  .settings(
    name := "example-impl",
    libraryDependencies ++= Seq(filters, macwire)
  )
  .enablePlugins(LagomScala)
  .dependsOn(`example-api`.jvm)

lazy val `client-js` = project
  .in(file("client-js"))
  .settings(commonJsSettings: _*)
  .settings(
    name := "client-js",
    libraryDependencies ++= Seq(
      "com.github.mliarakos.lagomjs" %%% "lagomjs-scaladsl-client" % lagomjsVersion,
      "com.lihaoyi"                  %%% "scalatags"               % "0.9.2",
      "org.scala-js"                 %%% "scalajs-dom"             % "1.1.0"
    ),
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(`example-api`.js)

lazy val `client-ui` = project
  .in(file("client-ui"))
  .settings(commonSettings: _*)
  .settings(
    name := "client-ui",
    libraryDependencies ++= Seq(
      "org.webjars" % "bootstrap" % "4.6.0-1",
      filters,
      macwire
    ),
    scalaJSProjects := Seq(`client-js`),
    pipelineStages in Assets := Seq(scalaJSPipeline)
  )
  .enablePlugins(PlayScala, LagomPlay)

lazy val `lagom-scalajs-example` = project
  .in(file("."))
  .settings(
    publish / skip := true,
    publishLocal / skip := true
  )
  .aggregate(
    `example-api`.jvm,
    `example-api`.js,
    `example-impl`,
    `client-js`,
    `client-ui`
  )
