package com.example.controllers

import java.sql.Date

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{PredefinedFromStringUnmarshallers, Unmarshaller}
import com.example.directives.VerifyToken
import com.example.models.{Book, BookJson, BookSearch}
import com.example.repository.BookRepository
import com.example.services.TokenService

import scala.concurrent.ExecutionContext

class BookController(val bookRepository: BookRepository, val tokenService: TokenService)(implicit val ec: ExecutionContext)
  extends BookJson
    with PredefinedFromStringUnmarshallers
    with VerifyToken {

  implicit val dateFromStringUnmarshaller: Unmarshaller[String, Date] =
    Unmarshaller.strict[String, Date] { string =>
      Date.valueOf(string)
    }

  val routes: Route = pathPrefix("books") {
    pathEndOrSingleSlash {
      get {
        parameters(('title.?, 'releaseDate.as[Date].?, 'categoryId.as[Long].?, 'author.?))
          .as(BookSearch) { bookSearch =>
            complete {
              bookRepository.search(bookSearch)
            }
          }
      } ~
        post {
          decodeRequest {
            entity(as[Book]) { book =>
              complete(StatusCodes.Created, bookRepository.create(book))
            }
          }
        }
    } ~
      pathPrefix(IntNumber) { id =>
        pathEndOrSingleSlash {
          get {
            verifyToken { _ =>
              onSuccess(bookRepository.findById(id)) {
                case Some(book) => complete(book)
                case None => complete(StatusCodes.NotFound)
              }
            }
          } ~
            delete {
              onSuccess(bookRepository.delete(id)) {
                case n if n > 0 => complete(StatusCodes.NoContent)
                case _ => complete(StatusCodes.NotFound)
              }
            }
        }

      }
  }

}
