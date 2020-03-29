package org.mliarakos.example.client

import java.util.Base64

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import org.mliarakos.example.api.{Ping, Pong}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, document}
import scalatags.JsDom.all._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

object Main {

  private val GREETING_ID = "greeting"
  private val HELLO_ID    = "hello"
  private val RANDOM_ID   = "random"
  private val PING_ID     = "ping"
  private val TICK_ID     = "tick"
  private val ECHO_ID     = "echo"
  private val BINARY_ID   = "binary"

  private implicit val materializer: Materializer = ExampleClient.application.materializer
  private val client                              = ExampleClient.client

  private def getInputValue(id: String): String = {
    document.getElementById(id).asInstanceOf[Input].value
  }

  private def displaySuccess(id: String, message: String): Unit = {
    val element = document.getElementById(id).asInstanceOf[HTMLElement]
    val alert   = div(`class` := "alert alert-success")(message).render
    element.appendChild(alert)
  }

  private def prepareSuccess(id: String): (HTMLElement, HTMLElement) = {
    val element = document.getElementById(id).asInstanceOf[HTMLElement]
    val alert   = div(`class` := "alert alert-success").render

    (element, alert)
  }

  private def displayException(id: String, exception: Throwable): Unit = {
    val message = exception match {
      case AjaxException(xhr)    => xhr.responseText
      case e: TransportException => s"${e.exceptionMessage.name}: ${e.exceptionMessage.detail} (${e.errorCode})"
      case _                     => s"${exception.getClass.getSimpleName}: ${exception.getMessage}"
    }
    val element = document.getElementById(id).asInstanceOf[HTMLElement]
    val alert   = div(`class` := "alert alert-danger")(message).render
    element.appendChild(alert)
  }

  private def greetingOnClick(event: Event): Unit = {
    client.greeting
      .invoke()
      .onComplete({
        case Success(response)  => displaySuccess(GREETING_ID, response)
        case Failure(exception) => displayException(GREETING_ID, exception)
      })
  }

  private def helloOnClick(event: Event): Unit = {
    val name = getInputValue("hello-name")
    client
      .hello(name)
      .invoke()
      .onComplete({
        case Success(response)  => displaySuccess(HELLO_ID, response)
        case Failure(exception) => displayException(HELLO_ID, exception)
      })
  }

  private def randomOnClick(event: Event): Unit = {
    val count = getInputValue("random-count")
    client
      .random(count.toInt)
      .invoke()
      .onComplete({
        case Success(response) =>
          val numbers = response.mkString(", ")
          displaySuccess(RANDOM_ID, numbers)
        case Failure(exception) =>
          displayException(RANDOM_ID, exception)
      })
  }

  private def pingOnClick(event: Event): Unit = {
    val name    = getInputValue("ping-name")
    val request = Ping(name)
    client.ping
      .invoke(request)
      .onComplete({
        case Success(Pong(message)) => displaySuccess(PING_ID, message)
        case Failure(exception)     => displayException(PING_ID, exception)
      })
  }

  private def tickOnClick(event: Event): Unit = {
    val message  = getInputValue("tick-message")
    val interval = getInputValue("tick-interval")
    client
      .tick(interval.toInt)
      .invoke(message)
      .onComplete({
        case Success(source) =>
          val (element, alert) = prepareSuccess(TICK_ID)
          source
            .runForeach(message => {
              if (!element.contains(alert)) element.appendChild(alert)
              val badge = span(`class` := "badge badge-light mr-1")(message).render
              alert.appendChild(badge)
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(TICK_ID, exception)
            })
        case Failure(exception) =>
          displayException(TICK_ID, exception)
      })
  }

  private def echoOnClick(event: Event): Unit = {
    val message = getInputValue("echo-message")
    val repeat  = getInputValue("echo-repeat")
    val source  = Source(List.fill(repeat.toInt)(message))
    client.echo
      .invoke(source)
      .onComplete({
        case Success(source) =>
          val (element, alert) = prepareSuccess(ECHO_ID)
          source
            .runForeach(message => {
              if (!element.contains(alert)) element.appendChild(alert)
              val badge = span(`class` := "badge badge-light mr-1")(message).render
              alert.appendChild(badge)
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(ECHO_ID, exception)
            })
        case Failure(exception) =>
          displayException(ECHO_ID, exception)
      })
  }

  private def binaryOnClick(event: Event): Unit = {
    client.binary
      .invoke()
      .onComplete({
        case Success(source) =>
          val (element, alert) = prepareSuccess(BINARY_ID)
          source
            .runForeach(message => {
              if (!element.contains(alert)) element.appendChild(alert)
              val base64Message = Base64.getEncoder.encodeToString(message.toArray)
              alert.textContent = base64Message
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(BINARY_ID, exception)
            })
        case Failure(exception) =>
          displayException(BINARY_ID, exception)
      })
  }

  def main(args: Array[String]): Unit = {
    val content =
      div(`class` := "container")(
        div(`class` := "row")(
          div(`class` := "col")(
            h1(`class` := "mb-4")("Lagom ScalaJS Examples"),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Greeting"),
              div(id := GREETING_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with no path parameters or request message."),
                p(`class` := "card-text")("The example service simply returns a static greeting."),
                hr,
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := greetingOnClick _)("Greeting")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Hello"),
              div(id := HELLO_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a string path parameter."),
                p(`class` := "card-text")(
                  "The ",
                  i("name"),
                  " is used as a path parameter and the example service returns a string greeting using the name."
                ),
                hr,
                div(`class` := "form-group")(
                  label("Name"),
                  input(id := "hello-name", `type` := "text", `class` := "form-control", value := "Lagom")
                ),
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := helloOnClick _)("Hello")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Random"),
              div(id := RANDOM_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a query parameter."),
                p(`class` := "card-text")(
                  "The example service returns ",
                  i("count"),
                  " random integers between 1 and 10 and will throw a custom exception if ",
                  i("count"),
                  " is not a positive integer."
                ),
                hr,
                div(`class` := "form-group")(
                  label("Count"),
                  input(id := "random-count", `type` := "number", `class` := "form-control", value := "10")
                ),
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := randomOnClick _)("Random")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Ping"),
              div(id := PING_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a serialized request and response message."),
                p(`class` := "card-text")(
                  "This example has the same result as the ",
                  i("Hello"),
                  " example, but uses serialized objects for the request and response instead of strings and path parameters."
                ),
                hr,
                div(`class` := "form-group")(
                  label("Name"),
                  input(id := "ping-name", `type` := "text", `class` := "form-control", value := "Lagom")
                ),
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := pingOnClick _)("Ping")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Tick"),
              div(id := TICK_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming response."),
                p(`class` := "card-text")(
                  "The example service returns a source that outputs the ",
                  i("message"),
                  " every ",
                  i("interval"),
                  " milliseconds and will throw a custom exception if ",
                  i("interval"),
                  " is not a positive integer."
                ),
                hr,
                div(`class` := "form-group")(
                  label("Message"),
                  input(id := "tick-message", `type` := "text", `class` := "form-control", value := "Lagom")
                ),
                div(`class` := "form-group")(
                  label("Interval"),
                  div(`class` := "input-group")(
                    input(id := "tick-interval", `type` := "number", `class` := "form-control", value := "500"),
                    div(`class` := "input-group-append")(
                      span(`class` := "input-group-text")("ms")
                    )
                  )
                ),
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := tickOnClick _)("Tick")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Echo"),
              div(id := ECHO_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming request and response."),
                p(`class` := "card-text")(
                  "A source is created that outputs the ",
                  i("message"),
                  " a total of ",
                  i("repeat"),
                  " times. The source is streamed to the example service and the service echos back all the messages it receives as another source."
                ),
                hr,
                div(`class` := "form-group")(
                  label("Message"),
                  input(id := "echo-message", `type` := "text", `class` := "form-control", value := "Lagom")
                ),
                div(`class` := "form-group")(
                  label("Repeat"),
                  input(id := "echo-repeat", `type` := "number", `class` := "form-control", min := "1", value := "10")
                ),
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := echoOnClick _)("Echo")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Binary"),
              div(id := BINARY_ID, `class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming binary response."),
                p(`class` := "card-text")("The example service returns a source that outputs random binary data."),
                hr,
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := binaryOnClick _)("Binary")
                )
              )
            )
          )
        )
      )

    document.body.appendChild(content.render)
  }

}
