package com.example.helpers

import java.sql.Date

import com.example.models.{Book, Category}
import com.example.repository.{BookRepository, CategoryRepository}

import scala.concurrent.{ExecutionContext, Future}

class BookSpecHelper(categoryRepository: CategoryRepository)(bookRepository: BookRepository)(implicit executionContext: ExecutionContext) {

  val category = Category(None, "Sci-Fi")
  val sciFiCategory = Category(None, "Sci-Fi")
  val techCategory = Category(None, "Technical")

  val bookFields = List(
    ("Akka in Action", techCategory.title, Date.valueOf("2016-09-01"), "Raymond Roestenburg, Rob Bakker, and Rob Williams"),
    ("Scala in Depth", techCategory.title, Date.valueOf("1993-01-01"), "Joshua D, Suereth"),
    ("Code Complete", techCategory.title, Date.valueOf("1895-01-01"), "Steve McConnell"),
    ("The Time Machine", sciFiCategory.title, Date.valueOf("1895-01-01"), "H.G. Wells"),
    ("Ninteen Eighty-Four", sciFiCategory.title, Date.valueOf("1949-01-01"), "George Orwell")
  )

  def bulkInsert(): Future[List[Book]] = {
    for {
      s <- categoryRepository.create(sciFiCategory)
      t <- categoryRepository.create(techCategory)
      b <- Future.sequence(bookFields.map { bookField =>
        val cId = if (bookField._2 == sciFiCategory.title) s.id.get else t.id.get
        val b = book(cId, bookField._1, bookField._3, bookField._4)
        bookRepository.create(b)
      })
    } yield b
  }

  def bulkDelete(): Future[Seq[Book]] = {
    for {
      books <- bookRepository.all
      _ <- Future.sequence(books.map(b => bookRepository.delete(b.id.get)))
      _ <- categoryRepository.delete(sciFiCategory.id.get)
      _ <- categoryRepository.delete(techCategory.id.get)
    } yield books
  }

  def book(categoryId: Long,
           title: String = "Murder in Ganymede",
           releaseDate: Date = Date.valueOf("1998-01-20"),
           author: String = "John Doe") =
    Book(None, title, releaseDate, categoryId, 3, author)

  def createAndDelete[T]()(assertion: Book => Future[T]): Future[T] = {
    categoryRepository.create(category) flatMap { c =>
      bookRepository.create(book(c.id.get)) flatMap { b =>
        val assertions = assertion(b)
        bookRepository.delete(b.id.get) flatMap { _ =>
          categoryRepository.delete(c.id.get) flatMap { _ => assertions }
        }
      }
    }
  }

}
