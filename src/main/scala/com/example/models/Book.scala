package com.example.models

import java.sql.Date

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

case class Book(
                 id: Option[String] = None,
                 title: String,
                 releaseDate: Date,
                 categoryId: String,
                 quantity: Int,
                 price: Double,
                 author: String
               )

object Book {
  // cannot do an import from FormatService as IntelliJ keeps removing it as unused
  implicit val sqlDateDecoder: Decoder[Date] = com.example.services.FormatService.decodeSqlDate
  implicit val sqlDateEncoder: Encoder[Date] = com.example.services.FormatService.encodeSqlDate

  implicit val authEncoder: Encoder[Book] = deriveEncoder[Book]
  implicit val authDecoder: Decoder[Book] = deriveDecoder[Book]
}

trait BookTable {

  class Books(tag: Tag) extends Table[Book](tag, "books") {
    def id = column[Option[String]]("id", O.PrimaryKey)

    def title = column[String]("title")

    def releaseDate = column[Date]("release_date")

    def categoryId = column[String]("category_id")

    def quantity = column[Int]("quantity")

    def price = column[Double]("price_usd")

    def author = column[String]("author")

    override def * = (id, title, releaseDate, categoryId, quantity, price, author) <> ((Book.apply _).tupled, Book.unapply)
  }

  protected val books = TableQuery[Books]
}