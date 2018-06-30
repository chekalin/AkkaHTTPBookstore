package com.example.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Auth(user: User, token: String)

object Auth {
  implicit val authEncoder: Encoder[Auth] = deriveEncoder[Auth]
  implicit val authDecoder: Decoder[Auth] = deriveDecoder[Auth]
}
