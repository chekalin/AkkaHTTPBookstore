package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MissingHeaderRejection
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.example.controllers.BookController
import com.example.helpers.{BookSpecHelper, CategorySpecHelper}
import com.example.models.{Book, BookJson, BookSearch, User}
import com.example.repository.{BookRepository, CategoryRepository, UserRepository}
import com.example.services.{ConfigService, FlywayService, MySqlService, TokenService}
import org.scalatest.{Assertion, AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class BookEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with BookJson {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new MySqlService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)
  val categorySpecHelper = new CategorySpecHelper(categoryRepository)
  val bookSpecHelper = new BookSpecHelper(categoryRepository)(bookRepository)
  val userRepository = new UserRepository(databaseService)
  val tokenService = new TokenService(userRepository)

  val bookController = new BookController(bookRepository, tokenService)

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
      val user = User(None, "user", "test@example.com", "password")

      def assertion(token: String): Future[Assertion] =
        Get("/books/10/") ~> addHeader("Authorization", token) ~> bookController.routes ~> check {
          status mustBe StatusCodes.NotFound
        }

      for {
        storedUser <- userRepository.create(user)
        result <- assertion(tokenService.createToken(storedUser))
        _ <- userRepository.delete(storedUser.id.get)
      } yield result

    }

    "reject request when there is no token in the request" in {
      Get("/books/10/") ~> bookController.routes ~> check {
        rejection mustBe MissingHeaderRejection("Authorization")
      }
    }

    "return unauthorized when there is an invalid token in the request" in {
      val invalidUser = User(Some(123), "Name", "Email", "Password")
      val invalidToken = tokenService.createToken(invalidUser)

      Get("/books/10/") ~> addHeader("Authorization", invalidToken) ~> bookController.routes ~> check {
        status mustBe StatusCodes.Unauthorized
      }
    }

    "return the book information when the token is valid" in {
      def assertion(token: String, bookId: Long): Future[Assertion] = {
        Get(s"/books/$bookId") ~> addHeader("Authorization", token) ~> bookController.routes ~> check {
          val book = responseAs[Book]
          book.title mustBe "Akka in Action"
          book.author mustBe "Raymond Roestenburg, Rob Bakker, and Rob Williams"
        }
      }

      val user = User(None, "name", "test@example.com", "password")
      val bookSearch = BookSearch(title = Some("Akka in Action"))

      for {
        storedUser <- userRepository.create(user)
        books <- bookRepository.search(bookSearch)
        result <- assertion(tokenService.createToken(storedUser), books.head.id.get)
        _ <- userRepository.delete(storedUser.id.get)
      } yield result
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
        val books = responseAs[List[Book]]
        books must have size bookSpecHelper.bookFields.size
      }
    }

    "return books with converted price" in {
      implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

      Get("/books/?currency=EUR") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK
        val books = responseAs[List[Book]]
        books.head.price must not be 35.0
      }
    }

    "return all books that conform to the query parameters sent" in {
      Get("/books?title=in&author=Ray") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK
        val books = responseAs[List[Book]]
        books must have size 1
      }
      Get("/books?title=The") ~> bookController.routes ~> check {
        status mustBe StatusCodes.OK
        val books = responseAs[List[Book]]
        books must have size 2
      }
    }
  }


}
