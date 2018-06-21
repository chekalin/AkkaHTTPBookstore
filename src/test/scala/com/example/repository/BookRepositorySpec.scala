package com.example.repository

import com.example.helpers.{BookSpecHelper, CategorySpecHelper}
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest._

class BookRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)
  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)
  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll(): Unit = flywayService.dropDatabase

  "A BookRepository" must {
    "be empty at the beginning" in {
      bookRepository.all map { bs => bs.size mustBe 0 }
    }
    "create valid books" in {
      bookSpecHelper.createAndDelete() { b =>
        b.id mustBe defined
        bookRepository.all map { cs => cs.size mustBe 1 }
      }
    }
    "not find a non-existent book" in {
      bookRepository.findById(0) map { b =>
        b must not be defined
      }
    }
    "not find an existing book" in {
      bookSpecHelper.createAndDelete() { b =>
        bookRepository.findById(b.id.get) map { book =>
          book mustBe defined
          book.get.title mustEqual b.title
        }
      }
    }
    "delete a category by id if it exists" in {
      for {
        category <- categoryRepository.create(bookSpecHelper.category)
        book <- bookRepository.create(bookSpecHelper.book(category.id.get))
        _ <- bookRepository.delete(book.id.get)
        books <- bookRepository.all
        _ <- categoryRepository.delete(category.id.get)
      } yield books.size mustBe 0
    }
  }

}
