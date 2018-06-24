package com.example.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.controllers.{BookController, CategoryController}
import com.example.repository.{BookRepository, CategoryRepository}

import scala.concurrent.ExecutionContext

class ApiService(categoryRepository: CategoryRepository, bookRepository: BookRepository, tokenService: TokenService)(implicit executionContext: ExecutionContext) {

  var categoryController: CategoryController = new CategoryController(categoryRepository)
  val bookController: BookController = new BookController(bookRepository, tokenService)

  def routes: Route = pathPrefix("api") {
    categoryController.routes ~
      bookController.routes
  }

}
