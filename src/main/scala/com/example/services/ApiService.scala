package com.example.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.controllers.{AuthController, BookController, CategoryController, UserController}
import com.example.repository.{AuthRepository, BookRepository, CategoryRepository, UserRepository}

import scala.concurrent.ExecutionContext

class ApiService(
                  categoryRepository: CategoryRepository,
                  bookRepository: BookRepository,
                  tokenService: TokenService,
                  userRepository: UserRepository,
                  authRepository: AuthRepository
                )(implicit executionContext: ExecutionContext) {

  var categoryController: CategoryController = new CategoryController(categoryRepository)
  val bookController: BookController = new BookController(bookRepository, tokenService)
  val userController: UserController = new UserController(userRepository, tokenService)
  val authController: AuthController = new AuthController(authRepository, tokenService)

  def routes: Route = pathPrefix("api") {
    categoryController.routes ~
      bookController.routes ~
      userController.routes ~
      authController.routes
  }

}
