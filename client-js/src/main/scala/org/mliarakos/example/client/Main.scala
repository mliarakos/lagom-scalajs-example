package org.mliarakos.example.client

import java.util.Base64

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import org.mliarakos.example.api.{Ping, Pong}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, document}
import scalatags.JsDom.all._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

object Main {

  private implicit val materializer: Materializer = ExampleClient.application.materializer

  private val client              = ExampleClient.client
  private val successAlertClasses = "alert alert-success mb-0"
  private val errorAlertClasses   = "alert alert-danger mb-0"

  private def getInputValue(id: String): String = {
    document.getElementById(id).asInstanceOf[Input].value
  }

  private def reset(element: HTMLElement): Unit = {
    element.textContent = ""
    for (i <- 0 until element.children.length) {
      val node = element.children(i)
      element.removeChild(node)
    }
  }

  private def getAlert(id: String): (HTMLElement, HTMLElement) = {
    val alert  = document.getElementById(id).asInstanceOf[HTMLElement]
    val output = alert.getElementsByTagName("p").namedItem("output").asInstanceOf[HTMLElement]
    (alert, output)
  }

  private def displaySuccess(message: String, alertId: String): Unit = {
    val (alert, output) = getAlert(alertId)
    alert.className = successAlertClasses
    output.textContent = message
  }

  private def displayException(exception: Throwable, alertId: String): Unit = {
    val message = exception match {
      case AjaxException(xhr) => xhr.responseText
      case _                  => s"${exception.getClass.getSimpleName}: ${exception.getMessage}"
    }

    val (alert, output) = getAlert(alertId)
    alert.className = errorAlertClasses
    output.textContent = message
  }

  private def greetingOnClick(event: Event): Unit = {
    client.greeting
      .invoke()
      .onComplete({
        case Success(response)  => displaySuccess(response, "greeting-alert")
        case Failure(exception) => displayException(exception, "greeting-alert")
      })
  }

  private def helloOnClick(event: Event): Unit = {
    val name = getInputValue("hello-name")
    client
      .hello(name)
      .invoke()
      .onComplete({
        case Success(response)  => displaySuccess(response, "hello-alert")
        case Failure(exception) => displayException(exception, "hello-alert")
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
          displaySuccess(numbers, "random-alert")
        case Failure(exception) =>
          displayException(exception, "random-alert")
      })
  }

  private def pingOnClick(event: Event): Unit = {
    val name    = getInputValue("ping-name")
    val request = Ping(name)
    client.ping
      .invoke(request)
      .onComplete({
        case Success(Pong(message)) => displaySuccess(message, "ping-alert")
        case Failure(exception)     => displayException(exception, "ping-alert")
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
          val (alert, output) = getAlert("tick-alert")
          reset(output)
          source
            .runForeach(message => {
              alert.className = successAlertClasses
              val badge = span(`class` := "badge badge-light mr-1")(message)
              output.appendChild(badge.render)
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(exception, "tick-alert")
            })
        case Failure(exception) =>
          displayException(exception, "tick-alert")
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
          val (alert, output) = getAlert("echo-alert")
          reset(output)
          source
            .runForeach(message => {
              alert.className = successAlertClasses
              val badge = span(`class` := "badge badge-light mr-1")(message)
              output.appendChild(badge.render)
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(exception, "echo-alert")
            })
        case Failure(exception) =>
          displayException(exception, "echo-alert")
      })
  }

  private def binaryOnClick(event: Event): Unit = {
    client.binary
      .invoke()
      .onComplete({
        case Success(source) =>
          source
            .runForeach(message => {
              val base64Message = Base64.getEncoder.encodeToString(message.toArray)
              displaySuccess(base64Message, "binary-alert")
            })
            .onComplete({
              case Success(_)         =>
              case Failure(exception) => displayException(exception, "binary-alert")
            })
        case Failure(exception) =>
          displayException(exception, "binary-alert")
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
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with no path parameters or request message."),
                p(`class` := "card-text")("The example service simply returns a static greeting."),
                hr,
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := greetingOnClick _)("Greeting")
                ),
                div(id := "greeting-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Hello"),
              div(`class` := "card-body")(
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
                ),
                div(id := "hello-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Random"),
              div(`class` := "card-body")(
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
                ),
                div(id := "random-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Ping"),
              div(`class` := "card-body")(
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
                ),
                div(id := "ping-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Tick"),
              div(`class` := "card-body")(
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
                ),
                div(id := "tick-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Echo"),
              div(`class` := "card-body")(
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
                ),
                div(id := "echo-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Binary"),
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming binary response."),
                p(`class` := "card-text")("The example service returns a source that outputs random binary data."),
                hr,
                div(`class` := "form-group")(
                  button(`class` := "btn btn-primary", onclick := binaryOnClick _)("Binary")
                ),
                div(id := "binary-alert", `class` := "d-none")(
                  p(name := "output", `class` := "mb-0")("")
                )
              )
            )
          )
        )
      )

    document.body.appendChild(content.render)
  }

}
