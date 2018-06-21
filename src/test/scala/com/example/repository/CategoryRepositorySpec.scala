package com.example.repository

import com.example.helpers.CategorySpecHelper
import com.example.services.{ConfigService, FlywayService, PostgresService}
import org.scalatest._

class CategoryRepositorySpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService {

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)
  val categoryRepository = new CategoryRepository(databaseService)
  val categorySpecHelper = new CategorySpecHelper(categoryRepository)

  override def beforeAll: Unit = flywayService.migrateDatabase

  override def afterAll(): Unit = flywayService.dropDatabase

  "A CategoryRepository" must {
    "be empty at the beginning" in {
      categoryRepository.all map { cs => cs.size mustBe 0 }
    }
    "create valid categories" in {
      categorySpecHelper.createAndDelete() { c =>
        c.id mustBe defined
        categoryRepository.all map { cs => cs.size mustBe 1 }
      }
    }
    "not find a category by title if it does not exist" in {
      categoryRepository.findByTitle("not a valid title") map { c =>
        c must not be defined
      }
    }
    "find a category by title if it exists" in {
      categorySpecHelper.createAndDelete() { c =>
        categoryRepository.findByTitle(c.title) map { c => c mustBe defined }
      }
    }
    "delete a category by id if it exists" in {
      categoryRepository.create(categorySpecHelper.category) flatMap { c =>
        categoryRepository.delete(c.id.get) flatMap { _ =>
          categoryRepository.all map { cs => cs.size mustBe 0 }
        }
      }
    }
  }

}
