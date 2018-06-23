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

  override def beforeAll {
    flywayService.migrateDatabase
  }

  override def afterAll {
    flywayService.dropDatabase
  }

  "Performing a book search" must {
    "return an empty list if there are no matches" in {
      bookSpecHelper.bulkInsertAndDelete { b =>
        val bookSearch = BookSearch(title = Some("Non existent book"))
        bookRepository.search(bookSearch).map { books =>
          books.size mustBe 0
        }
      }
    }

    "return matching books by title" in {
      bookSpecHelper.bulkInsertAndDelete { b =>
        val bookSearch = BookSearch(title = Some("Akka"))
        bookRepository.search(bookSearch).map { books =>
          books.size mustBe 1
          books.head.title mustBe bookSpecHelper.bookFields.head._1
        }

        val bookSearchMultiple = BookSearch(title = Some("The"))
        bookRepository.search(bookSearchMultiple).map { books =>
          books.size mustBe 2
        }
      }
    }

    "return books by release date" in {
      bookSpecHelper.bulkInsertAndDelete { b =>
        val bookSearch = BookSearch(releaseDate = Some(Date.valueOf("1993-01-01")))
        bookRepository.search(bookSearch).map { books =>
          books.size mustBe 1
          books.head.title mustBe bookSpecHelper.bookFields(2)._1
        }
      }
    }

    "return books by category" in {
      bookSpecHelper.bulkInsertAndDelete { b =>

        for {
          Some(category) <- categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
          books <- bookRepository.search(BookSearch(categoryId = Some(category.id.get)))
        } yield books.size mustBe 3
      }
    }

    "return the books by author" in {
      bookSpecHelper.bulkInsertAndDelete { b =>

        val bookSearch = BookSearch(author = Some(". We"))
        bookRepository.search(bookSearch).map { books =>
          books.size mustBe 2
        }
      }
    }

    "return correctly the expected books when combining searches" in {
      bookSpecHelper.bulkInsertAndDelete { b =>

        for {
          Some(category) <- categoryRepository.findByTitle(bookSpecHelper.sciFiCategory.title)
          books <- bookRepository.search(BookSearch(categoryId = category.id, title = Some("Scala")))
        } yield books.size mustBe 0

        val bookSearch = BookSearch(author = Some("H.G."), title = Some("The"))
        bookRepository.search(bookSearch).map { books =>
          books.size mustBe 2
        }
      }
    }

    "return all books for empty search" in {
      bookSpecHelper.bulkInsertAndDelete { b =>
        bookRepository.search(BookSearch()).map { books =>
          books.size mustBe 6
        }
      }
    }

  }

}
