package org.mliarakos.example.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.mliarakos.example.api.{ExampleService, Pong}

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

  override def tick(interval: Int) = ServerServiceCall { message =>
    val source = Source.tick(interval.milliseconds, interval.milliseconds, message).mapMaterializedValue(_ => NotUsed)
    Future.successful(source)
  }

  override def echo = ServerServiceCall { source =>
    Future.successful(source)
  }

}
