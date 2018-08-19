package controllers

import play.api.Environment
import play.api.mvc.{AbstractController, ControllerComponents}

class Main(components: ControllerComponents, environment: Environment) extends AbstractController(components) {

  def index = Action {
    Ok(views.html.main(environment.mode))
  }

}
