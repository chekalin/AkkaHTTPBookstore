package com.example.services

import java.sql.Date

import cats.syntax.either._
import io.circe.{Decoder, Encoder}

object FormatService {
  implicit val encodeSqlDate: Encoder[Date] = Encoder.encodeString.contramap[Date](_.toString)
  implicit val decodeSqlDate: Decoder[Date] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(Date.valueOf(str)).leftMap(t => "Date")
  }
}
