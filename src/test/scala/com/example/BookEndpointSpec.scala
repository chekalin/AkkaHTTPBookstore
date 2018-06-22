package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.controllers.BookController
import com.example.helpers.{BookSpecHelper, CategorySpecHelper}
import com.example.models.{Book, BookJson}
import com.example.repository.{BookRepository, CategoryRepository}
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

class BookEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with BookJson {

  override implicit val executor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)
  val categorySpecHelper = new CategorySpecHelper(categoryRepository)
  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)

  val bookController = new BookController(bookRepository)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
    bookSpecHelper.bulkInsert()
  }

  override def afterAll: Unit = {
    bookSpecHelper.bulkDelete()
    flywayService.dropDatabase
  }

  "A Book Endpoint" must {
    "create a book" in {
      categoryRepository.create(bookSpecHelper.category).map { c =>
        Post("/books/", bookSpecHelper.book(c.id.get)) ~> bookController.routes ~> check {
          status mustBe StatusCodes.Created

          val book = responseAs[Book]
          bookRepository.delete(book.id.get)
          categoryRepository.delete(c.id.get)
          book.id mustBe defined
          book.title mustBe "Murder in Ganymede"
        }
      }
    }
    "return NotFound when we try to get a non existent book" in {
      Get("/books/10/") ~> bookController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "return book by id" in {
      categoryRepository.create(categorySpecHelper.category) flatMap { c =>
        bookRepository.create(bookSpecHelper.book(c.id.get)) flatMap { b =>
          Get(s"/books/${b.id.get}/") ~> bookController.routes ~> check {
            bookRepository.delete(b.id.get)
            categoryRepository.delete(c.id.get)
            val book = responseAs[Book]
            book.id mustBe b.id
            book.title mustBe "Murder in Ganymede"
          }
        }
      }
    }
    "return NotFound when we try to delete a non existent book" in {
      Delete("/books/10/") ~> bookController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "return NoContent when we delete existent book" in {
      categoryRepository.create(categorySpecHelper.category) flatMap { c =>
        bookRepository.create(bookSpecHelper.book(c.id.get)) flatMap { b =>
          Delete(s"/books/${b.id.get}/") ~> bookController.routes ~> check {
            categoryRepository.delete(c.id.get)
            status mustBe StatusCodes.NoContent
          }
        }
      }
    }
    "return all books when no query parameters are sent" in {
      Get("/books/") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK
        val books= responseAs[List[Book]]
        books must have size bookSpecHelper.bookFields.size
      }
    }
  }


}
