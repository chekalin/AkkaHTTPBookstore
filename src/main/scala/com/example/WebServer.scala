package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.repository.{BookRepository, CategoryRepository}
import com.example.services._

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object WebServer extends App
  with ConfigService
  with WebApi {
  override implicit val system: ActorSystem = ActorSystem("Akka_HTTP_Bookstore")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executor: ExecutionContext = system.dispatcher

  val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabase
  val databaseService = new PostgresService(jdbcUrl, dbUser, dbPassword)

  val categoryRepository = new CategoryRepository(databaseService)
  val bookRepository = new BookRepository(databaseService)

  val apiService = new ApiService(categoryRepository, bookRepository)

  val bindingFuture = Http().bindAndHandle(apiService.routes, httpHost, httpPort)

  println(s"Server online at $httpHost:$httpPort")
  println("Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
