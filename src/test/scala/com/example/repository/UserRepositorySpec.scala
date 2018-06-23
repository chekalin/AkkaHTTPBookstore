package com.example.repository

import com.example.models.User
import com.example.services.{ConfigService, FlywayService, PostgresService}
import com.github.t3hnar.bcrypt._
import org.scalatest._

class UserRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)
  val userRepository = new UserRepository(databaseService)

  val user = User(None, "Name", "email", "password")

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll(): Unit = flywayService.dropDatabase

  "A UserRepository" must {
    "be empty at the beginning" in {
      userRepository.all.map { us =>
        us.size mustBe 0
      }
    }
    "create valid users" in {
      for {
        u <- userRepository.create(user)
        all <- userRepository.all
        _ <- userRepository.delete(u.id.get)
      } yield {
        u.id mustBe defined
        user.password.isBcrypted(u.password) mustBe true
        all must have size 1
      }
    }
    "not find user by email if user does not exist" in {
      userRepository.findByEmail("admin@google.com").map { user =>
        user must not be defined
      }
    }
    "find user by email if user exists" in {
      for {
        u <- userRepository.create(user)
        Some(found) <- userRepository.findByEmail(user.email)
        _ <- userRepository.delete(u.id.get)
      } yield {
        found.id mustBe u.id
      }
    }

    "not find user by id if user does not exist" in {
      userRepository.findById(123).map { user =>
        user must not be defined
      }
    }

    "find user by id if user exists" in {
      for {
        u <- userRepository.create(user)
        Some(found) <- userRepository.findById(u.id.get)
        _ <- userRepository.delete(u.id.get)
      } yield {
        found.id mustBe u.id
      }
    }

    "delete user by id if user exists" in {
      for {
        u <- userRepository.create(user)
        _ <- userRepository.delete(u.id.get)
        all <- userRepository.all
      } yield {
        all must have size 0
      }
    }
  }

}
