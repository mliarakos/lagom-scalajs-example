package org.mliarakos.example.api

import play.api.libs.json.{Format, Json}

case class Ping(name: String)

object Ping {
  implicit val format: Format[Ping] = Json.format[Ping]
}
