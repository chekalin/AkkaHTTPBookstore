package com.example.helpers

import java.sql.Date

import com.example.models.{Book, Category}
import com.example.repository.{BookRepository, CategoryRepository}
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class BookSpecHelper(categoryRepository: CategoryRepository)(bookRepository: BookRepository)(implicit executionContext: ExecutionContext) {

  val category = Category(None, "Category")
  val sciFiCategory = Category(None, "Sci-Fi")
  val techCategory = Category(None, "Technical")

  val bookFields = List(
    ("Akka in Action", techCategory.title, Date.valueOf("2016-09-01"), 35.0, "Raymond Roestenburg, Rob Bakker, and Rob Williams"),
    ("Scala in Depth", techCategory.title, Date.valueOf("2012-01-01"), 40.0, "Joshua D. Suereth"),
    ("Code Complete", techCategory.title, Date.valueOf("1993-01-01"), 55.0, "Steve McConnell"),
    ("The Time Machine", sciFiCategory.title, Date.valueOf("1895-01-01"), 15.0, "H.G. Wells"),
    ("The Invisible Man", sciFiCategory.title, Date.valueOf("1897-01-01"), 15.0, "H.G. Wells"),
    ("Nineteen Eighty-Four", sciFiCategory.title, Date.valueOf("1949-01-01"), 12.0, "George Orwell")
  )

  def bulkInsert(): Future[Seq[Book]] = {
    for {
      s <- categoryRepository.create(sciFiCategory)
      t <- categoryRepository.create(techCategory)
      b <- bookRepository.bulkCreate(bookFields.map { bookField =>
        val cId = if (bookField._2 == sciFiCategory.title) s.id.get else t.id.get
        book(cId, bookField._1, bookField._3, bookField._4, bookField._5)
      })
    } yield b
  }

  def bulkInsertAndDelete(assertion: Seq[Book] => Future[Assertion]): Future[Assertion] = {
    bulkInsert().flatMap { books =>
      val a = assertion(books)
      bulkDelete().flatMap(_ => a)
    }
  }

  def bulkDelete(): Future[Seq[Book]] = {
    for {
      books <- bookRepository.all
      _ <- Future.sequence(books.map(b => bookRepository.delete(b.id.get)))
      Some(sciFi) <- categoryRepository.findByTitle(sciFiCategory.title)
      Some(tech) <- categoryRepository.findByTitle(techCategory.title)
      _ <- categoryRepository.delete(sciFi.id.get)
      _ <- categoryRepository.delete(tech.id.get)
    } yield books
  }

  def book(categoryId: Long,
           title: String = "Murder in Ganymede",
           releaseDate: Date = Date.valueOf("1998-01-20"),
           price: Double = 10.0,
           author: String = "John Doe") =
    Book(None, title, releaseDate, categoryId, 3, price, author)

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
