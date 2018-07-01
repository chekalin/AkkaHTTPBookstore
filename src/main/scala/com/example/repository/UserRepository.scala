package com.example.repository

import java.util.UUID

import com.example.models.{User, UserTable}
import com.example.services.DatabaseService
import com.github.t3hnar.bcrypt._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(databaseService: DatabaseService)(implicit val executionContext: ExecutionContext) extends UserTable {

  import databaseService._
  import databaseService.driver.api._

  def create(user: User): Future[User] = {
    val secureUserWithId = user.copy(
      password = user.password.bcrypt,
      id = Some(UUID.randomUUID().toString)
    )
    db.run(users += secureUserWithId).map(_ => secureUserWithId)
  }

  def delete(id: String): Future[Int] = db.run(users.filter(_.id === id).delete)

  def all: Future[Seq[User]] = db.run(users.result)

  def findById(id: String): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  def findByEmail(email: String): Future[Option[User]] = db.run(users.filter(_.email === email).result.headOption)

}
