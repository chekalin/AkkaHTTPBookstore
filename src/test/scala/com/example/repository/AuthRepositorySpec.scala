package com.example.repository

import com.example.models.{Credentials, User}
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest._

class AuthRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)
  val userRepository = new UserRepository(databaseService)
  val authRepository = new AuthRepository(databaseService)

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll(): Unit = flywayService.dropDatabase

  "An AuthRepository" must {
    "return None if user with email does not exist" in {
      authRepository.findByCredentials(Credentials("whatever", "password")) map { result =>
        result must not be defined
      }
    }
    "return None if password does not match" in {
      for {
        user <- userRepository.create(User(None, "admin", "admin@example.com", "secret"))
        result <- authRepository.findByCredentials(Credentials("admin@example.com", "password"))
        _ <- userRepository.delete(user.id.get)
      } yield {
        result must not be defined
      }
    }
    "return user if email and password match" in {
      for {
        user <- userRepository.create(User(None, "admin", "admin@example.com", "secret"))
        result <- authRepository.findByCredentials(Credentials("admin@example.com", "secret"))
        _ <- userRepository.delete(user.id.get)
      } yield {
        result mustBe defined
        result.get.id.get mustBe user.id.get
      }
    }
  }

}
