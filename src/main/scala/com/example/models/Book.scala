package com.example.models

import java.sql.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Book(
                 id: Option[Long] = None,
                 title: String,
                 releaseDate: Date,
                 categoryId: Long,
                 quantity: Int,
                 price: Double,
                 author: String
               )

trait BookJson extends SprayJsonSupport with DefaultJsonProtocol {

  import com.example.services.FormatService._

  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat7(Book.apply)
}

trait BookTable {

  class Books(tag: Tag) extends Table[Book](tag, "books") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def releaseDate = column[Date]("release_date")

    def categoryId = column[Long]("category_id")

    def quantity = column[Int]("quantity")

    def price = column[Double]("price_usd")

    def author = column[String]("author")

    override def * = (id, title, releaseDate, categoryId, quantity, price, author) <> ((Book.apply _).tupled, Book.unapply)
  }

  protected val books = TableQuery[Books]
}