package com.example.repository

import java.util.UUID

import com.example.models._
import com.example.services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class CategoryRepository(val databaseService: DatabaseService)(implicit executor: ExecutionContext) extends CategoryTable {

  import databaseService._
  import databaseService.driver.api._

  def all: Future[Seq[Category]] = db.run(categories.result)

  def withGeneratedId(category: Category): Category =
    category.copy(id = Some(UUID.randomUUID().toString))

  def create(category: Category): Future[Category] = {
    val categoryWithId = withGeneratedId(category)
    db.run(categories += categoryWithId)
      .map(_ => categoryWithId)
  }

  def findByTitle(title: String): Future[Option[Category]] = db.run(categories.filter(_.title === title).result.headOption)

  def delete(id: String): Future[Int] = db.run(categories.filter(_.id === id).delete)

}
