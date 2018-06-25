package com.example

import java.sql.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.models.{Book, Category}
import com.example.repository.{AuthRepository, BookRepository, CategoryRepository, UserRepository}
import com.example.services._
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object WebServer extends App
  with ConfigService
  with WebApi {
  override implicit val system: ActorSystem = ActorSystem("Akka_HTTP_Bookstore")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executor: ExecutionContext = system.dispatcher

  val logger = Logger("App")

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabase
  val databaseService = new MySqlService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)
  val userRepository = new UserRepository(databaseService)
  val authRepository = new AuthRepository(databaseService)
  val tokenService = new TokenService(userRepository)

  val sciFiCategory = Category(None, "Sci-Fi")
  val techCategory = Category(None, "Technical")

  logger.info("initializing DB with test data...")
  for {
    Category(Some(sciFiCategoryId), _) <- categoryRepository.create(sciFiCategory)
    Category(Some(techCategoryId), _) <- categoryRepository.create(techCategory)
  } {
    logger.info("Creating books...")
    bookRepository.bulkCreate(Seq(
      Book(None, "Akka in Action", Date.valueOf("2016-09-01"), techCategoryId, 2, 35.0, "Raymond Roestenburg, Rob Bakker, and Rob Williams"),
      Book(None, "Scala in Depth", Date.valueOf("2012-01-01"), techCategoryId, 5, 40.0, "Joshua D. Suereth"),
      Book(None, "Code Complete", Date.valueOf("1993-01-01"), techCategoryId, 8, 55.0, "Steve McConnell"),
      Book(None, "The Time Machine", Date.valueOf("1895-01-01"), sciFiCategoryId, 2, 15.0, "H.G. Wells"),
      Book(None, "The Invisible Man", Date.valueOf("1897-01-01"), sciFiCategoryId, 3, 15.0, "H.G. Wells"),
      Book(None, "Nineteen Eighty-Four", Date.valueOf("1949-01-01"), sciFiCategoryId, 1, 12.0, "George Orwell")
    ))
  }

  val apiService = new ApiService(categoryRepository, bookRepository, tokenService, userRepository, authRepository)

  val bindingFuture = Http().bindAndHandle(apiService.routes, httpHost, httpPort)

  println(s"Server online at $httpHost:$httpPort")
  println("Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
