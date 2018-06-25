package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.controllers.AuthController
import com.example.models._
import com.example.repository.{AuthRepository, UserRepository}
import com.example.services.{ConfigService, FlywayService, MySqlService, TokenService}
import org.scalatest.{Assertion, AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.{ExecutionContextExecutor, Future}

class AuthEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with AuthJson
  with CredentialsJson {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new MySqlService(jdbcUrl, dbUser, dbPassword)

  val userRepository = new UserRepository(databaseService)
  val authRepository = new AuthRepository(databaseService)
  val tokenService = new TokenService(userRepository)
  val authController = new AuthController(authRepository, tokenService)

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll: Unit = flywayService.dropDatabase

  "An Auth endpoint" must {
    "return Auth when login successful" in {
      val user = User(None, "name", "email", "password")
      val credentials = Credentials("email", "password")

      def assert(credentials: Credentials): Future[Assertion] = {
        Post("/auth", credentials) ~> authController.routes ~> check {
          status mustBe StatusCodes.OK
          val auth = responseAs[Auth]
          auth.user.email mustBe user.email
          tokenService.isTokenValidForMember(auth.token, auth.user) map { result =>
            result mustBe true
          }
        }
      }

      for {
        user <- userRepository.create(user)
        result <- assert(credentials)
        _ <- userRepository.delete(user.id.get)
      } yield result
    }

    "return unauthorised status code whrn login fails" in {
      val credentials = Credentials("hacker", "123")

      Post("/auth", credentials) ~> authController.routes ~> check {
        status mustBe StatusCodes.Unauthorized
      }

    }
  }

}
