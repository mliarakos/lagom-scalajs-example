import com.softwaremill.macwire._
import controllers.{AssetsComponents, Main}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes

class ClientUi(context: Context)
    extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    with HttpFiltersComponents {

  override lazy val router: Router = {
    val prefix = "/"
    wire[Routes]
  }

  lazy val main: Main = wire[Main]
}

class ClientUiLoader extends ApplicationLoader {
  override def load(context: Context): Application = context.environment.mode match {
    case Mode.Dev => new ClientUi(context).application
    case _        => new ClientUi(context).application
  }
}
