package com.example.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class User(id: Option[Long], name: String, email: String, password: String)

trait UserJson extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)
}

trait UserTable {

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def email = column[String]("email")

    def password = column[String]("password")

    def * = (id, name, email, password) <> ((User.apply _).tupled, User.unapply)
  }

  protected val users = TableQuery[Users]
}
