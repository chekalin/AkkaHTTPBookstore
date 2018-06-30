package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.controllers.UserController
import com.example.models.User
import com.example.repository.UserRepository
import com.example.services.{ConfigService, FlywayService, MySqlService, TokenService}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{Assertion, AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.{ExecutionContextExecutor, Future}

class UserEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with FailFastCirceSupport {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new MySqlService(jdbcUrl, dbUser, dbPassword)

  val userRepository = new UserRepository(databaseService)

  val tokenService = new TokenService(userRepository)
  val userController = new UserController(userRepository, tokenService)

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

    "return Unauthorized when no user is found by id" in {
      val invalidUser = User(Some(123), "name", "test@example.com", "password")
      val invalidToken = tokenService.createToken(invalidUser)
      Get("/users/10") ~> addHeader("Authorization", invalidToken) ~> userController.routes ~> check {
        status mustBe StatusCodes.Unauthorized
      }
    }

    "return user data when user found by id" in {
      def assert(user: User): Future[Assertion] = {
        val token = tokenService.createToken(user)
        Get(s"/users/${user.id.get}") ~> addHeader("Authorization", token) ~> userController.routes ~> check {
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

    "return Unauthorized when the user requested is not the same as token" in {
      def assert(user: User, token: String): Future[Assertion] = {
        Get(s"/users/${user.id.get}") ~> addHeader("Authorization", token) ~> userController.routes ~> check {
          status mustBe StatusCodes.Unauthorized
        }
      }

      val user2 = User(None, "name2", "email2", "password2")

      for {
        u <- userRepository.create(user)
        u2 <- userRepository.create(user2)
        result <- assert(u, tokenService.createToken(u2))
        _ <- userRepository.delete(u.id.get)
        _ <- userRepository.delete(u2.id.get)
      } yield result
    }

  }

}
