package com.example.controllers

import java.sql.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{PredefinedFromStringUnmarshallers, Unmarshaller}
import akka.stream.Materializer
import com.example.directives.VerifyToken
import com.example.models.{Book, BookSearch}
import com.example.repository.BookRepository
import com.example.services.{CurrencyService, TokenService}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class BookController(val bookRepository: BookRepository, val tokenService: TokenService)
                    (implicit val ec: ExecutionContext, actorSystem: ActorSystem, materializer: Materializer)
  extends FailFastCirceSupport
    with PredefinedFromStringUnmarshallers
    with VerifyToken {

  implicit val dateFromStringUnmarshaller: Unmarshaller[String, Date] =
    Unmarshaller.strict[String, Date] { string =>
      Date.valueOf(string)
    }

  val DefaultCurrency = "USD"

  val routes: Route = pathPrefix("books") {
    pathEndOrSingleSlash {
      get {
        parameter('currency.?) { currency =>
          parameters(('title.?, 'releaseDate.as[Date].?, 'categoryId.as[Long].?, 'author.?))
            .as(BookSearch.apply) { bookSearch =>
              onSuccess(bookRepository.search(bookSearch)) { books =>
                currency match {
                  case Some(currencyCode) if currencyCode != DefaultCurrency =>
                    onSuccess(CurrencyService.getRates) {
                      case Some(rates) => complete(books.map { book => book.copy(price = book.price * rates(currencyCode)) })
                      case None => complete(StatusCodes.ServiceUnavailable)
                    }
                  case _ => complete(books)
                }
              }
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
