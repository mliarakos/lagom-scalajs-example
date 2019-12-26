package org.mliarakos.example.client

import java.net.URI

import com.lightbend.lagom.scaladsl.client.{StandaloneLagomClientFactory, StaticServiceLocatorComponents}
import org.mliarakos.example.api.ExampleService
import org.scalajs.dom.window

class ExampleClientApplication(hostname: String = window.location.hostname)
    extends StandaloneLagomClientFactory("example-client")
    with StaticServiceLocatorComponents {
  override def staticServiceUri: URI = URI.create(s"http://$hostname:58440")
}

object ExampleClient {
  val application: ExampleClientApplication = new ExampleClientApplication()
  val client: ExampleService                = application.serviceClient.implement[ExampleService]
}
