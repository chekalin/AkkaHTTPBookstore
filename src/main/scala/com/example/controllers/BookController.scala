package com.example.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.example.repository.BookRepository
import akka.http.scaladsl.server.Directives._
import com.example.models.{Book, BookJson, Category}

class BookController(bookRepository: BookRepository) extends BookJson {

  val routes: Route = pathPrefix("books") {
    pathEndOrSingleSlash {
      post {
        decodeRequest {
          entity(as[Book]) { book =>
              complete(StatusCodes.Created, bookRepository.create(book))
          }
        }
      }
    }
  }

}
