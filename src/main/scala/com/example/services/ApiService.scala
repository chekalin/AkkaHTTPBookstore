package com.example.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.Materializer
import com.example.controllers.{AuthController, BookController, CategoryController, UserController}
import com.example.repository.{AuthRepository, BookRepository, CategoryRepository, UserRepository}

import scala.concurrent.ExecutionContext

class ApiService(
                  categoryRepository: CategoryRepository,
                  bookRepository: BookRepository,
                  tokenService: TokenService,
                  userRepository: UserRepository,
                  authRepository: AuthRepository
                )(
                  implicit executionContext: ExecutionContext,
                  actorSystem: ActorSystem,
                  materializer: Materializer
                ) extends HttpApp {

  var categoryController: CategoryController = new CategoryController(categoryRepository)
  val bookController: BookController = new BookController(bookRepository, tokenService)
  val userController: UserController = new UserController(userRepository, tokenService)
  val authController: AuthController = new AuthController(authRepository, tokenService)

  override def routes: Route = pathPrefix("api") {
    categoryController.routes ~
      bookController.routes ~
      userController.routes ~
      authController.routes
  }

}
