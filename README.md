# Lagom Scala.js Example

An example of how to use [lagom.js](https://github.com/mliarakos/lagom-js) to interact with a [Lagom](https://www.lagomframework.com/) service in JavaScript.

### Getting Started

1. Clone the example repo:

   ```sh
   git clone https://github.com/mliarakos/lagom-scalajs-example.git
   ```
1. Start the example:

   ```sh
   cd lagom-scalajs-example
   sbt runAll
   ```
1. Open http://localhost:53781 in a browser

The example is configured for both Scala 2.12 and Scala 2.13, with Scala 2.12 as the default. To run using Scala 2.13:

```sh
sbt ++2.13.1 runAll
```

### Project Structure

The example is made up of four projects: `example-api`, `example-impl`, `client-js`, and `client-ui`. The example projects are the standard Lagom projects for a service api and service implementation. The only difference is that the `example-api` project is configured to be cross-compiled into JavaScript using Scala.js. Lagom.js enables a standard Lagom service api to be cross-compiled without any changes. The example service provides several end points to demo the functionality of the lagom.js service client.

The client projects are the web front end that uses lagom.js to interact with the example service. The `client-js` project is the core of the demo. It is a single page JavaScript app that uses the lagom.js client of the example service. The `client-ui` project is a minimal Play app that simply serves the single page application. The Play app is not Lagom aware.

### Service Client

A basic standalone Lagom application is created using the `StaticServiceLocator`. For this demo the Lagom dev mode port of the example service is hard coded as the URI of the service:

```scala
class ExampleClientApplication(hostname: String = window.location.hostname)
    extends StandaloneLagomClientFactory("example-client")
    with StaticServiceLocatorComponents {
  override def staticServiceUri: URI = URI.create(s"http://$hostname:58440")
}
```

The application is used to build a client for the example service:

```scala
  val application = new ExampleClientApplication()
  val client      = application.serviceClient.implement[ExampleService]
```

The client can now be used to interact with the service. 

### Examples

#### Greeting

This example calls the `greeting` end point of the service:

```scala
def greeting: ServiceCall[NotUsed, String]

restCall(Method.GET, "/greeting", greeting)
```

This is a service call that uses no path parameters or request messages and returns a basic String response. The service simply returns a static greeting.

In the `client-js` project the service client is used the same way you would in a standard Scala Lagom project:

```scala
client.greeting.invoke().onComplete({
  case Success(message)   => // display message
  case Failure(exception) => // handle exception
})
``` 

#### Hello

This example calls the `hello` end point of the service:

```scala
def hello(name: String): ServiceCall[NotUsed, String]

restCall(Method.GET, "/hello/:name", hello _)
```

This is a service call that uses a String path parameter. The service returns a greeting using the user provided name parameter.

In the `client-js` project:

```scala
client.hello(name).invoke().onComplete({
  case Success(message)   => // display message
  case Failure(exception) => // handle exception
})
```

#### Random

This example calls the `random` end point of the service:

```scala
def random(count: Int): ServiceCall[NotUsed, Seq[Int]]

restCall(Method.GET, "/random?count", random _)
```

This is a service call that uses a query parameter. The service returns `count` random integers between 1 and 10.

In the `client-js` project:

```scala
val count = 10
client.random(count).invoke().onComplete({
  case Success(response)  => // display response
  case Failure(exception) => // handle exception
})
```

The service will throw a [custom exception](#custom-service-exceptions) if `count` is not a positive integer.

#### Ping

This example calls the `ping` end point of the service:

```scala
def ping: ServiceCall[Ping, Pong]

restCall(Method.POST, "/ping", ping)
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
  case Success(Pong(message)) => // display message
  case Failure(exception)     => // handle exception
})
```

#### Tick

This example calls the `tick` end point of the service:

```scala
def tick(interval: Int): ServiceCall[String, Source[String, NotUsed]]

pathCall("/tick/:interval", tick _)
```

This is a service call that has a streaming response. The service returns a `Source` that outputs the provided user message every `interval` milliseconds.

In the `client-js` project:

```scala
val message = "Lagom"
val interval = 500
client.tick(interval).invoke(message)
  .flatMap(source => {
    source.runForeach(message => /* display message */)
  })
  .onComplete({
    case Success(_)         => // handle completion of the source
    case Failure(exception) => // handle exception
  })
```

The service will throw a [custom exception](#custom-service-exceptions) if `interval` is not a positive integer.

#### Echo

This example calls the `echo` end point of the service:

```scala
def echo: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

pathCall("/echo", echo)
```

This is a service call that uses a streaming request and response. The client creates a `Source` that continuously repeats the provided user message. The `Source` is streamed to the service and the service returns another `Source` that echos back all the messages. The client stops the returned source after `limit` messages.

In the `client-js` project:

```scala
val message = "Lagom"
val limit = 10
val source = Source.tick(Duration.Zero, Duration(500, MILLISECONDS), message).mapMaterializedValue(_ => NotUsed)
client.echo.invoke(source)
  .flatMap(source => {
    source.take(limit).runForeach(message => /* display message */)
  })
  .onComplete({
    case Success(_)         => // handle completion of the source
    case Failure(exception) => // handle exception
  })
```

#### Binary

This example calls the `binary` end point of the service:

```scala
def binary: ServiceCall[NotUsed, Source[ByteString, NotUsed]]

pathCall("/binary", binary)
```

This is a service call that has a streaming binary response. The service returns a `Source` that outputs random binary data. The data is streamed as binary in the underlying WebSocket.

In the `client-js` project:

```scala
client.binary.invoke()
  .flatMap(source => {
    source.runForeach(message => /* display message */)
  })
  .onComplete({
    case Success(_)         => // handle completion of the source
    case Failure(exception) => // handle exception
  })
```

### Custom Service Exceptions

The service uses a custom exception that can be sent to and handled by the client. It is the `NonPositiveIntegerException`, which is used by several examples that require positive integer parameters. As described in the [Lagom documentation](https://www.lagomframework.com/documentation/latest/scala/ServiceErrorHandling.html), the exception extends `TransportException` and the service uses a custom `ExceptionSerializer`.

The service provides two exception serializers in the service API: `ExampleExceptionSerializer` (the default) and `ExampleEnvironmentExceptionSerializer` (an alternate). Both extend the Lagom built-in `DefaultExceptionSerializer`, which controls the error detail level based on the environment (production vs. development). The difference between them is how they select the environment and where they are configured in the application.

The `ExampleExceptionSerializer` is configured in the service definition:

```scala
trait ExampleService extends Service {
  override def descriptor: Descriptor = {
    import Service._
    named("example")
      .withCalls(
        // call definitions
      )
      .withExceptionSerializer(ExampleExceptionSerializer)
  }
}
```

This configures both the server and client at the same time. However, configuring the exception serializer in the service definition requires a static environment choice. The `ExampleExceptionSerializer` chooses the development environment because this is a demo. However, this should not be done in production to prevent leaking error details.

The `ExampleEnvironmentExceptionSerializer` is configured in the application definition and uses the environment of the application, allowing for a dynamic environment definition. However, since there are two application definitions (one for the server and one for the client), it must be configured in two separate places. The `ExampleApplication` in `example-impl` and the `ExampleClient` in `client-js` show how `ExampleEnvironmentExceptionSerializer` would be configured. 
