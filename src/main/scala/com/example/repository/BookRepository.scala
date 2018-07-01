package com.example.repository

import java.util.UUID

import com.example.models._
import com.example.services.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class BookRepository(val databaseService: DatabaseService)(implicit executor: ExecutionContext) extends BookTable {

  import databaseService._
  import databaseService.driver.api._

  def findById(id: String): Future[Option[Book]] = db.run(books.filter(_.id === id).result.headOption)

  def all: Future[Seq[Book]] = db.run(books.result)

  def delete(id: String): Future[Int] = db.run(books.filter(_.id === id).delete)

  def withGeneratedId(book: Book): Book = book.copy(id = Some(UUID.randomUUID().toString))

  def create(book: Book): Future[Book] = {
    val bookWithId = withGeneratedId(book)
    db.run(books += bookWithId).map(_ => bookWithId)
  }

  def bulkCreate(bookSeq: Seq[Book]): Future[Seq[Book]] = {
    val bookSeqWithIds = bookSeq.map(withGeneratedId)
    db.run(books ++= bookSeqWithIds).map(_ => bookSeqWithIds)
  }

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
