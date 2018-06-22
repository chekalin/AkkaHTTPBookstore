package com.example.repository

import java.sql.Date

import com.example.helpers.BookSpecHelper
import com.example.models.BookSearch
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest._

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
    bookSpecHelper.bulkInsert()
  }

  override def afterAll(): Unit = {
    bookSpecHelper.bulkDelete()
    flywayService.dropDatabase
  }

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
        books.head.title mustBe bookSpecHelper.bookFields.head._1
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
        books.head.title mustBe bookSpecHelper.bookFields(1)._1
      }
    }

    "return books by category" in {
      for {
        Some(category) <- categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
        books <- bookRepository.search(BookSearch(categoryId = Some(category.id.get)))
      } yield books.size mustBe 2
    }

    "return the books by author" in {
      val bookSearch = BookSearch(author = Some(". We"))
      bookRepository.search(bookSearch).map { books =>
        books.size mustBe 1
      }
    }

    "return correctly the expected books when combining searches" in {
      for {
        Some(category) <- categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
        books <- bookRepository.search(BookSearch(categoryId = category.id, title = Some("Scala")))
      } yield books.size mustBe 0

      val bookSearch = BookSearch(author = Some("H.G."), title = Some("The"))
      bookRepository.search(bookSearch).map { books =>
        books.foreach(println)
        books.size mustBe 1
      }
    }

    "return all books for empty search" in {
      bookRepository.search(BookSearch()).map { books =>
        books.size mustBe 5
      }
    }

  }

}
