package com.example.repository

import com.example.models.{User, UserTable}
import com.example.services.DatabaseService
import com.github.t3hnar.bcrypt._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(databaseService: DatabaseService)(implicit val executionContext: ExecutionContext) extends UserTable {

  import databaseService._
  import databaseService.driver.api._

  def create(user: User): Future[User] = {
    val secureUser = user.copy(password = user.password.bcrypt)
    db.run(users returning users.map(_.id) += secureUser).map(id => secureUser.copy(id = id))
  }

  def delete(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def all: Future[Seq[User]] = db.run(users.result)

  def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  def findByEmail(email: String): Future[Option[User]] = db.run(users.filter(_.email === email).result.headOption)

}
