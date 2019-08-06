# Lagom ScalaJs Example

An example of how to use the Lagom.js client prototype.

### Getting Started

The Lagom.js client is currently a prototype. You'll need to publish it locally to test it out.

1. Clone the Lagom.js prototype Git repo:

   ```
   git clone https://github.com/mliarakos/lagom.js.git
   ```
1. Publish Lagom.js locally:

   ```
   sbt publishLocal
   ```
1. Clone the example repo:

   ```
   git clone -b lagomjs https://github.com/mliarakos/lagom-scalajs-example.git
   ```
1. Start the example:

   ```
   sbt runAll
   ```
1. Open http://localhost:53781 in a browser

### Project Structure

The example is made up of four projects: `example-api`, `example-impl`, `client-js`, and `client-ui`. The example projects are the standard Lagom projects for a service api and service implementation. The only difference is that the `example-api` project is configured as ScalaJS cross-compilable. The Lagom ScalaJS client enables a standard Lagom service api to be cross-compilable without any changes. The example service provides five end points to demo the functionality of the Lagom ScalaJs client.

The client projects are the web front end that uses the Lagom ScalaJS client to interact with the example service. The `client-js` project is the core of the demo. It is a single page JavaScript app that uses the ScalaJS client of the example service. The `client-ui` project is a minimal Play app that simply serves the single page application. The Play app is not Lagom aware.

### Client Examples

#### Greeting

This example calls the `greeting` end point of the service:

```scala
def greeting: ServiceCall[NotUsed, String]
```

This is a service call that uses no path parameters or request messages and returns a basic String response. The service simply returns a static greeting.

In the `client-js` project the service client is used the same way you would in a standard Scala Lagom project:

```scala
client.greeting.invoke().onComplete({
  case Success(message) => // display message
  case Failure(exception) => // handle exception
)}
``` 

#### Hello

This example calls the `hello` end point of the service:

```scala
def hello(name: String): ServiceCall[NotUsed, String]
```

This is a service call that uses a String path parameter. The service returns a greeting using the user provided name parameter.

In the `client-js` project:

```scala
client.hello(name).invoke().onComplete({
  case Success(message) => // display message
  case Failure(exception) => // handle exception
)}
```

#### Ping

This example calls the `ping` end point of the service:

```scala
def ping: ServiceCall[Ping, Pong]
```

This is a service call that uses serialized request and response messages. The messages are basic wrappers around Strings:

```scala
case class Ping (name: String)

object Ping {
  implicit val format: Format[Ping] = Json.format[Ping]
}

case class Pong(message: String)

object Pong {
  implicit val format: Format[Pong] = Json.format[Pong]
}
```

Like the Hello example, the service returns a greeting using the user provided name. The difference is that this example uses serialized requests and responses. The service returns a greeting in the `Pong` response using the name in the `Ping` request.

In the `client-js` project:

```scala
val request = Ping(name)
client.ping.invoke(request).onComplete({
  case Success(Pong(message)) => //display message
  case Failure(exception) => // handle exception
)}
```

#### Tick

This example calls the `tick` end point of the service:

```scala
def tick(interval: Int): ServiceCall[String, Source[String, NotUsed]]
```

This is a service call that has a streaming response. The service returns a `Source` that outputs the provided user message every `interval` milliseconds.

In the `client-js` project:

```scala
val message = "Lagom"
val interval = 500
client.tick(interval).invoke(message).onComplete({
  case Success(response) => 
    response.runForeach(message => /* display message */)
  case Failure(exception) => // handle exception
})
```

#### Echo

This example calls the `echo` end point of the service:

```scala
def echo: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
```

This is a service call that uses a streaming request and response. The client creates a `Source` that repeats a message for a user defined number of times. The `Source` is streamed to the service and the service returns another `Source` that echos back all the messages.

In the `client-js` project:

```scala
val message = "Lagom"
val repeat = 10
val source = Source(List.fill(repeat)(message))
client.echo.invoke(source).onComplete({
  case Success(response) => 
    response.runForeach(message => /* display message */)
  case Failure(exception) => // handle exception
})
```
