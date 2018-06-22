package com.example.repository

import com.example.models._
import com.example.services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class BookRepository(val databaseService: DatabaseService)(implicit executor: ExecutionContext) extends BookTable {

  import databaseService._
  import databaseService.driver.api._

  def findById(id: Long): Future[Option[Book]] = db.run(books.filter(_.id === id).result.headOption)

  def all: Future[Seq[Book]] = db.run(books.result)

  def delete(id: Long): Future[Int] = db.run(books.filter(_.id === id).delete)

  def create(book: Book): Future[Book] = db.run(books returning books += book)

  def search(bookSearch: BookSearch): Future[Seq[Book]] = {
    val query = books.filter { book =>
      List(
        bookSearch.title.map(t => book.title like s"%$t%"),
        bookSearch.releaseDate.map(book.releaseDate === _),
        bookSearch.categoryId.map(book.categoryId === _),
        bookSearch.author.map(a => book.author like s"%$a%")
      )
        .collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ && _)
        .getOrElse(true: Rep[Boolean])
    }
    db.run(query.result)
  }
}
