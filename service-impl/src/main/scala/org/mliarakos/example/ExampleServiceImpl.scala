package org.mliarakos.example

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.Future
import scala.concurrent.duration._

class ExampleServiceImpl extends ExampleService {

  override def greeting = ServerServiceCall { _ =>
    Future.successful(s"Welcome!")
  }

  override def hello(name: String) = ServerServiceCall { _ =>
    Future.successful(s"Hello $name!")
  }

  override def ping = ServerServiceCall { request =>
    val message = s"Hello ${request.name}!"
    Future.successful(Pong(message))
  }

  override def tick(intervalMs: Int) = ServerServiceCall { tickMessage =>
    Future.successful(Source.tick(
      intervalMs.milliseconds, intervalMs.milliseconds, tickMessage
    ).mapMaterializedValue(_ => NotUsed))
  }

  override def echo = ServerServiceCall { source =>
    Future.successful(source)
  }

}
