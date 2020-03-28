package org.mliarakos.example.api

import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, TransportErrorCode, TransportException}
import play.api.{Environment, Mode}

import scala.collection.immutable._

class ExampleEnvironmentExceptionSerializer(environment: Environment) extends DefaultExceptionSerializer(environment) {
  private val exceptions: Map[String, (TransportErrorCode, ExceptionMessage) => TransportException] = Map(
    NonPositiveIntegerException.name -> NonPositiveIntegerException.apply
  )

  protected override def fromCodeAndMessage(
      transportErrorCode: TransportErrorCode,
      exceptionMessage: ExceptionMessage
  ): Throwable = {
    exceptions
      .getOrElse(exceptionMessage.name, super.fromCodeAndMessage _)
      .apply(transportErrorCode, exceptionMessage)
  }
}

object ExampleEnvironmentExceptionSerializer {
  val devEnvironment: Environment = Environment.simple(mode = Mode.Dev)
}
