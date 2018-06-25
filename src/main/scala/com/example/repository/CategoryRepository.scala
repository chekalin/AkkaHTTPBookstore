package com.example.repository

import com.example.models._
import com.example.services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class CategoryRepository(val databaseService: DatabaseService)(implicit executor: ExecutionContext) extends CategoryTable {

  import databaseService._
  import databaseService.driver.api._

  def all: Future[Seq[Category]] = db.run(categories.result)

  def create(category: Category): Future[Category] = db
    .run(categories returning categories.map(_.id) += category)
    .map(id => category.copy(id = id))

  def findByTitle(title: String): Future[Option[Category]] = db.run(categories.filter(_.title === title).result.headOption)

  def delete(id: Long): Future[Int] = db.run(categories.filter(_.id === id).delete)

}
