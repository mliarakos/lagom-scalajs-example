package org.mliarakos.example.api

import play.api.libs.json.{Format, Json}

case class Pong(message: String)

object Pong {
  implicit val format: Format[Pong] = Json.format[Pong]
}
