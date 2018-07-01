package com.example.models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

case class User(id: Option[String], name: String, email: String, password: String)

object User {
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
}

trait UserTable {

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Option[String]]("id", O.PrimaryKey)

    def name = column[String]("name")

    def email = column[String]("email")

    def password = column[String]("password")

    def * = (id, name, email, password) <> ((User.apply _).tupled, User.unapply)
  }

  protected val users = TableQuery[Users]
}
