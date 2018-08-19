package org.mliarakos.example.client

import java.net.URI

import com.lightbend.lagom.scaladsl.client.{LagomClientApplication, StaticServiceLocatorComponents}
import org.mliarakos.example.ExampleService
import org.scalajs.dom.window

class ExampleClientApplication(hostname: String = window.location.hostname)
  extends LagomClientApplication("example-client") with StaticServiceLocatorComponents {
  override def staticServiceUri: URI = URI.create(s"http://$hostname:58936")
}

object ExampleClient {
  val app = new ExampleClientApplication()
  val client: ExampleService = app.serviceClient.implement[ExampleService]
}
