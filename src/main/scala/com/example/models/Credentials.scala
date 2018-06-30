package com.example.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Credentials(email: String, password: String)

object Credentials {
  implicit val credentialsEncoder: Encoder[Credentials] = deriveEncoder[Credentials]
  implicit val credentialsDecoder: Decoder[Credentials] = deriveDecoder[Credentials]
}
