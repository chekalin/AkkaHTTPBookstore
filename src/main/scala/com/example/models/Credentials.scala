package com.example.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Credentials(email: String, password: String)

trait CredentialsJson extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val credentialsFormat: RootJsonFormat[Credentials] = jsonFormat2(Credentials.apply)
}
