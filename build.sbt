import sbt.Keys.{scalaVersion, version}
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

scalaVersion in ThisBuild := "2.11.12"

lazy val commonSettings = Seq(
  organization := "org.mliarakos.lagom-scalajs-example",
  version := "0.1.0"
)

lazy val commonJsSettings = commonSettings ++ Seq(
  scalacOptions += "-P:scalajs:sjsDefinedByDefault"
)

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided

lazy val `service-api` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full)
  .in(file("service-api"))
  .settings(
    name := "service-api"
  )
  .jvmSettings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .jsSettings(commonJsSettings: _*)
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lightbend.lagom" %%% "lagom-scaladsl-api" % "1.5.0-SNAPSHOT"
    )
  )

lazy val `service-api-jvm` = `service-api`.jvm

lazy val `service-api-js` = `service-api`.js

lazy val `service-impl` = project.in(file("service-impl"))
  .settings(commonSettings: _*)
  .settings(
    name := "service-impl",
    libraryDependencies ++= Seq(
      filters,
      lagomScaladslPersistenceCassandra,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`service-api-jvm`)

lazy val `client-js` = project.in(file("client-js"))
  .settings(commonJsSettings: _*)
  .settings(
    name := "client-js",
    libraryDependencies ++= Seq(
      "com.lightbend.lagom" %%% "lagom-scaladsl-client" % "1.5.0-SNAPSHOT",
      "com.lihaoyi" %%% "scalatags" % "0.6.7",
      "org.scala-js" %%% "scalajs-dom" % "0.9.6"
    ),
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(`service-api-js`)

lazy val `client-ui` = project.in(file("client-ui"))
  .settings(commonSettings: _*)
  .settings(
    name := "client-ui",
    libraryDependencies ++= Seq(
      filters,
      macwire
    ),
    scalaJSProjects := Seq(`client-js`),
    pipelineStages in Assets := Seq(scalaJSPipeline)
  )
  .enablePlugins(PlayScala, LagomPlay)

lazy val `lagom-scalajs-example` = project.in(file("."))
  .aggregate(`service-api-jvm`, `service-api-js`, `client-js`, `client-ui`)
  .settings(
    publish := {},
    publishLocal := {}
  )
