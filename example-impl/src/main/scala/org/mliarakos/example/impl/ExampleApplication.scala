package org.mliarakos.example.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import com.softwaremill.macwire._
import org.mliarakos.example.api.ExampleService
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSComponents

import scala.collection.immutable._

abstract class ExampleApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents
    with CORSComponents {

  override val httpFilters: Seq[EssentialFilter] = Seq(corsFilter)

  override lazy val lagomServer: LagomServer =
    serverFor[ExampleService](wire[ExampleServiceImpl])
}

class ExampleApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ExampleApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ExampleApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ExampleService])

}
