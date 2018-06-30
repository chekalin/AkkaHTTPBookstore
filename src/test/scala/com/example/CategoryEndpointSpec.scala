package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.controllers.CategoryController
import com.example.helpers.CategorySpecHelper
import com.example.models.Category
import com.example.repository.CategoryRepository
import com.example.services.{ConfigService, FlywayService, MySqlService}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.ExecutionContextExecutor

class CategoryEndpointSpec extends AsyncWordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ConfigService
  with WebApi
  with ScalatestRouteTest
  with FailFastCirceSupport {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  val databaseService = new MySqlService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val categorySpecHelper = new CategorySpecHelper(categoryRepository)

  val categoryController = new CategoryController(categoryRepository)

  override def beforeAll: Unit = {
    flywayService.migrateDatabase
  }

  override def afterAll: Unit = {
    flywayService.dropDatabase
  }

  "A CategoryEndpoint" must {
    "return an empty list at the beginning" in {
      Get("/categories/") ~> categoryController.routes ~> check {
        status mustBe StatusCodes.OK
        val categories = responseAs[List[Category]]
        categories must have size 0
      }
    }
    "return all the categories when there is at least one" in {
      categorySpecHelper.createAndDelete() { c =>
        Get("/categories/") ~> categoryController.routes ~> check {
          status mustBe StatusCodes.OK
          val categories = responseAs[List[Category]]
          categories must have size 1
        }
      }
    }
    "return BadRequest with repeated titles" in {
      categorySpecHelper.createAndDelete() { c =>
        Post("/categories/", categorySpecHelper.category) ~> categoryController.routes ~> check {
          status mustBe StatusCodes.BadRequest
        }
      }
    }
    "create a category" in {
      Post("/categories/", categorySpecHelper.category) ~> categoryController.routes ~> check {
        status mustBe StatusCodes.Created

        val category = responseAs[Category]
        categoryRepository.delete(category.id.get)
        category.id mustBe defined
        category.title mustBe categorySpecHelper.category.title
      }
    }
    "return NotFound when we try to delete a non existent category" in {
      Delete("/categories/10/") ~> categoryController.routes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "return NoContent when we delete existent category" in {
      categoryRepository.create(categorySpecHelper.category) flatMap { c =>
        Delete(s"/categories/${c.id.get}/") ~> categoryController.routes ~> check {
          status mustBe StatusCodes.NoContent
        }
      }
    }
  }


}
