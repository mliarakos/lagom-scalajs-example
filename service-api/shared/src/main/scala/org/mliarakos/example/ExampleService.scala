package org.mliarakos.example

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.Method

trait ExampleService extends Service {

  def greeting: ServiceCall[NotUsed, String]

  def hello(name: String): ServiceCall[NotUsed, String]

  def ping: ServiceCall[Ping, Pong]

  def tick(interval: Int): ServiceCall[String, Source[String, NotUsed]]

  def echo: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override def descriptor: Descriptor = {
    import Service._
    named("example").withCalls(
      restCall(Method.GET, "/greeting", greeting),
      restCall(Method.GET, "/hello/:name", hello _),
      restCall(Method.POST, "/ping", ping),
      pathCall("/tick/:interval", tick _),
      pathCall("/echo", echo)
    ).withAcls(
      ServiceAcl.forMethodAndPathRegex(Method.OPTIONS, "/.*")
    ).withAutoAcl(true)
  }

}
