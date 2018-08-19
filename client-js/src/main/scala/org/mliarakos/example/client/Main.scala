package org.mliarakos.example.client

import akka.stream.scaladsl.Source
import org.mliarakos.example.Ping
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.html.Input
import org.scalajs.dom.{Event, console, document}
import scalatags.JsDom.all._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

object Main {

  private val client = ExampleClient.client

  private def handleException(exception: Throwable): Unit = {
    exception match {
      case AjaxException(xhr) =>
        console.error(xhr)
      case _ =>
        console.error(exception.toString)
    }
  }

  private def greetingOnClick(event: Event): Unit = {
    client.greeting.invoke().onComplete({
      case Success(response) =>
        document.getElementById("greeting").textContent = response
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def helloOnClick(event: Event): Unit = {
    val name = document.getElementById("name").asInstanceOf[Input].value
    client.hello(name).invoke().onComplete({
      case Success(response) =>
        document.getElementById("hello").textContent = response
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def pingOnClick(event: Event): Unit = {
    val name = document.getElementById("ping").asInstanceOf[Input].value
    val request = Ping(name)
    client.ping.invoke(request).onComplete({
      case Success(response) =>
        document.getElementById("pong").textContent = response.message
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def tickOnClick(event: Event): Unit = {
    val interval = document.getElementById("interval").asInstanceOf[Input].value
    val request = "Hello"
    client.tick(interval.toInt).invoke(request).onComplete({
      case Success(response) =>
        response.runForeach(message => {
          val text = document.createTextNode(s"$message ")
          document.getElementById("tick").appendChild(text)
        })(ExampleClient.app.materializer)
      case Failure(exception) =>
        handleException(exception)
    })
  }

  private def echoOnClick(event: Event): Unit = {
    val message = document.getElementById("echo-input").asInstanceOf[Input].value
    val source = Source(List.fill(10)(message))
    client.echo.invoke(source).onComplete({
      case Success(response) =>
        response.runForeach(message => {
          val text = document.createTextNode(message)
          document.getElementById("echo-output").appendChild(text)
        })(ExampleClient.app.materializer)
      case Failure(exception) =>
        handleException(exception)
    })
  }

  def main(args: Array[String]): Unit = {
    val content =
      div(
        div(
          button(onclick := greetingOnClick _)("Greeting"),
          span(id := "greeting")
        ),
        div(
          input(`type` := "text", id := "name"),
          button(onclick := helloOnClick _)("Hello"),
          span(id := "hello")
        ),
        div(
          input(`type` := "text", id := "ping"),
          button(onclick := pingOnClick _)("Ping"),
          span(id := "pong")
        ),
        div(
          input(`type` := "text", id := "interval"),
          button(onclick := tickOnClick _)("Tick"),
          div(id := "tick")
        ),
        div(
          input(`type` := "text", id := "echo-input"),
          button(onclick := echoOnClick _)("Echo"),
          div(id := "echo-output")
        )
      )

    document.body.appendChild(content.render)
  }

}
