package com.example.models

import java.sql.Date

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class BookSearch(
                       title: Option[String] = None,
                       releaseDate: Option[Date] = None,
                       categoryId: Option[Long] = None,
                       author: Option[String] = None
                     )

object BookSearch {
  // cannot do an import from FormatService as IntelliJ keeps removing it as unused
  implicit val sqlDateDecoder: Decoder[Date] = com.example.services.FormatService.decodeSqlDate
  implicit val sqlDateEncoder: Encoder[Date] = com.example.services.FormatService.encodeSqlDate

  implicit val bookSearchEncoder: Encoder[BookSearch] = deriveEncoder[BookSearch]
  implicit val bookSearchDecoder: Decoder[BookSearch] = deriveDecoder[BookSearch]
}

