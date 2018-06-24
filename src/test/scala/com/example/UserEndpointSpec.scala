package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.controllers.UserController
import com.example.models.{User, UserJson}
import com.example.repository.UserRepository
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest.{Assertion, AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.{ExecutionContextExecutor, Future}

class UserEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with UserJson {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val userRepository = new UserRepository(databaseService)

  val userController = new UserController(userRepository)

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll: Unit = flywayService.dropDatabase

  val user = User(None, "name", "email", "password")

  "A User endpoint" must {
    "return BadRequest with repeated emails" in {
      def assert(user: User): Future[Assertion] = {
        Post("/users", user) ~> userController.routes ~> check {
          status mustBe StatusCodes.BadRequest
        }
      }

      for {
        u <- userRepository.create(user)
        result <- assert(user)
        _ <- userRepository.delete(u.id.get)
      } yield result
    }

    "create user if user does not exist" in {
      var userResponse: User = null
      Post("/users", user) ~> userController.routes ~> check {
        status mustBe StatusCodes.Created
        userResponse = responseAs[User]
        userRepository.delete(userResponse.id.get) map { _ =>
          userResponse.id mustBe defined
        }
      }
    }

    "return NotFound when no user is found by id" in {
      Get("/users/10") ~> userController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "return user data when user found by id" in {
      def assert(user: User): Future[Assertion] = {
        Get(s"/users/${user.id.get}") ~> userController.routes ~> check {
          status mustBe StatusCodes.OK
          val userResponse = responseAs[User]
          userResponse.name mustBe user.name
        }
      }

      for {
        u <- userRepository.create(user)
        result <- assert(u)
        _ <- userRepository.delete(u.id.get)
      } yield result
    }
  }

}
