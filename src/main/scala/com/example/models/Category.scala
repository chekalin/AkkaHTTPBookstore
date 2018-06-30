package com.example.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import slick.jdbc.MySQLProfile.api._

case class Category(id: Option[Long] = None, title: String)

object Category {
  implicit val categoryEncoder: Encoder[Category] = deriveEncoder[Category]
  implicit val categoryDecoder: Decoder[Category] = deriveDecoder[Category]
}

trait CategoryTable {

  class Categories(tag: Tag) extends Table[Category](tag, "categories") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def * = (id, title) <> ((Category.apply _).tupled, Category.unapply)
  }

  protected val categories = TableQuery[Categories]

}