import sbt.Keys.{scalaVersion, version}
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

scalaVersion in ThisBuild := "2.12.9"

lazy val commonSettings = Seq(
  organization := "com.github.mliarakos.lagom-scalajs-example",
  version := "0.1.0"
)

lazy val commonJsSettings = commonSettings ++ Seq(
  resolvers += Resolver.sonatypeRepo("snapshots"),
  scalacOptions += "-P:scalajs:sjsDefinedByDefault"
)

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % Provided

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
      "com.github.mliarakos.lagomjs" %%% "lagomjs-scaladsl-api" % "0.1.0-1.5.1-SNAPSHOT"
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
      "com.github.mliarakos.lagomjs" %%% "lagomjs-scaladsl-client" % "0.1.0-1.5.1-SNAPSHOT",
      "com.lihaoyi"                  %%% "scalatags"               % "0.6.8",
      "org.scala-js"                 %%% "scalajs-dom"             % "0.9.7"
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
      "org.webjars" % "bootstrap" % "4.3.1",
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
