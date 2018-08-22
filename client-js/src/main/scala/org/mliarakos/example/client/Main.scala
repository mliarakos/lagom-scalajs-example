package org.mliarakos.example.client

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import org.mliarakos.example.api.{Ping, Pong}
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, console, document}
import scalatags.JsDom.all._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

object Main {

  private implicit val materializer: Materializer = ExampleClient.application.materializer

  private val client = ExampleClient.client
  private val successAlertClasses = "alert alert-success mb-0"

  private def handleException(exception: Throwable): Unit = {
    exception match {
      case AjaxException(xhr) => console.error(xhr)
      case _ => console.error(exception.toString)
    }
  }

  private def greetingOnClick(event: Event): Unit = {
    client.greeting.invoke().onComplete({
      case Success(response) =>
        val alert = document.getElementById("greeting-alert").asInstanceOf[HTMLElement]
        alert.className = successAlertClasses
        alert.getElementsByTagName("p").namedItem("greeting-output").textContent = response
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def helloOnClick(event: Event): Unit = {
    val name = document.getElementById("hello-name").asInstanceOf[Input].value
    client.hello(name).invoke().onComplete({
      case Success(response) =>
        val alert = document.getElementById("hello-alert").asInstanceOf[HTMLElement]
        alert.className = successAlertClasses
        alert.getElementsByTagName("p").namedItem("hello-output").textContent = response
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def pingOnClick(event: Event): Unit = {
    val name = document.getElementById("ping-name").asInstanceOf[Input].value
    val request = Ping(name)
    client.ping.invoke(request).onComplete({
      case Success(Pong(message)) =>
        val alert = document.getElementById("ping-alert").asInstanceOf[HTMLElement]
        alert.className = successAlertClasses
        alert.getElementsByTagName("p").namedItem("ping-output").textContent = message
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def tickOnClick(event: Event): Unit = {
    val message = document.getElementById("tick-message").asInstanceOf[Input].value
    val interval = document.getElementById("tick-interval").asInstanceOf[Input].value
    client.tick(interval.toInt).invoke(message).onComplete({
      case Success(response) =>
        val alert = document.getElementById("tick-alert").asInstanceOf[HTMLElement]
        val output = alert.getElementsByTagName("p").namedItem("tick-output")
        alert.className = successAlertClasses
        response.runForeach(message => {
          val badge = span(`class` := "badge badge-light mr-1")(message)
          output.appendChild(badge.render)
        })
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def echoOnClick(event: Event): Unit = {
    val message = document.getElementById("echo-message").asInstanceOf[Input].value
    val repeat = document.getElementById("echo-repeat").asInstanceOf[Input].value
    val source = Source(List.fill(repeat.toInt)(message))
    client.echo.invoke(source).onComplete({
      case Success(response) =>
        val alert = document.getElementById("echo-alert").asInstanceOf[HTMLElement]
        val output = alert.getElementsByTagName("p").namedItem("echo-output")
        alert.className = successAlertClasses
        response.runForeach(message => {
          val badge = span(`class` := "badge badge-light mr-1")(message)
          output.appendChild(badge.render)
        })
      case Failure(exception) =>
        handleException(exception)
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
                  p(id := "greeting-output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Hello"),
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with a string path parameter."),
                p(`class` := "card-text")(
                  "The ", i("name"), " is used as a path parameter and the example service returns a string greeting using the name."
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
                  p(id := "hello-output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Ping"),
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with a serialized request and response message."),
                p(`class` := "card-text")(
                  "This example has the same result as the ", i("Hello"), " example, but uses serialized objects for the request and response instead of strings and path parameters."
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
                  p(id := "ping-output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Tick"),
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming response."),
                p(`class` := "card-text")("The example service returns a source that outputs the ", i("message"), " every ", i("interval"), " milliseconds."),
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
                  p(id := "tick-output", `class` := "mb-0")("")
                )
              )
            ),
            div(`class` := "card mb-4")(
              h5(`class` := "card-header")("Echo"),
              div(`class` := "card-body")(
                p(`class` := "card-text")("A service call with a streaming request and response."),
                p(`class` := "card-text")(
                  "A source is created that outputs the ", i("message"), " a total of ", i("repeat"), " times. The source is streamed to the example service and the service echos back all the messages it receives as another source."
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
                  p(id := "echo-output", `class` := "mb-0")("")
                )
              )
            )
          )
        )
      )

    document.body.appendChild(content.render)
  }

}
