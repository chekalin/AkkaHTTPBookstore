package com.example.repository

import java.sql.Date

import com.example.helpers.BookSpecHelper
import com.example.models.{BookSearch, Category}
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest._

import scala.concurrent.Future

class BookSearchSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)
  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)
  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase

    for {
      s <- categoryRepository.create(sciFiCategory)
      t <- categoryRepository.create(techCategory)
      b <- Future.sequence(bookFields.map { bookField =>
        val cId = if (bookField._2 == sciFiCategory.title) s.id.get else t.id.get
        val book = bookSpecHelper.book(cId, bookField._1, bookField._3, bookField._4)
        bookRepository.create(book)
      })
    } yield b
  }

  override def afterAll(): Unit = {
    for {
      books <- bookRepository.all
      _ <- Future.sequence(books.map(b => bookRepository.delete(b.id.get)))
      _ <- categoryRepository.delete(sciFiCategory.id.get)
      _ <- categoryRepository.delete(techCategory.id.get)
    } yield books

    flywayService.dropDatabase
  }

  val sciFiCategory = Category(None, "Sci-Fi")
  val techCategory = Category(None, "Technical")

  val bookFields = List(
    ("Akka in Action", techCategory.title, Date.valueOf("2016-09-01"), "Raymond Roestenburg, Rob Bakker, and Rob Williams"),
    ("Scala in Depth", techCategory.title, Date.valueOf("1993-01-01"), "Joshua D, Suereth"),
    ("Code Complete", techCategory.title, Date.valueOf("1895-01-01"), "Steve McConnell"),
    ("The Time Machine", sciFiCategory.title, Date.valueOf("1895-01-01"), "H.G. Wells"),
    ("Ninteen Eighty-Four", sciFiCategory.title, Date.valueOf("1949-01-01"), "George Orwell")
  )

  "Performing a book search" must {
    "return an empty list if there are no matches" in {
      val bookSearch = BookSearch(title = Some("Non existent book"))
      bookRepository.search(bookSearch).map { books =>
        books.size mustBe 0
      }
    }

    "return matching books by title" in {
      val bookSearch = BookSearch(title = Some("Akka"))
      bookRepository.search(bookSearch).map { books =>
        books.size mustBe 1
        books.head.title mustBe bookFields.head._1
      }

      val bookSearchMultiple = BookSearch(title = Some(" in "))
      bookRepository.search(bookSearchMultiple).map { books =>
        books.size mustBe 2
      }
    }

    "return books by release date" in {
      val bookSearch = BookSearch(releaseDate = Some(Date.valueOf("1993-01-01")))
      bookRepository.search(bookSearch).map { books =>
        books.size mustBe 1
        books.head.title mustBe bookFields(1)._1
      }
    }

    "return books by category" in {
      for {
        Some(category) <- categoryRepository.findByTitle(sciFiCategory.title)
        books <- bookRepository.search(BookSearch(categoryId = Some(category.id.get)))
      } yield books.size mustBe 2
    }

    "return the books by author" in {
      val bookSearch = BookSearch(author = Some(". We"))
      bookRepository.search(bookSearch).map { books =>
        books.size mustBe 1
      }
    }
  }

}
