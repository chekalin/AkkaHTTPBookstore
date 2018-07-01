package com.example.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.models.Category
import com.example.repository.CategoryRepository
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

class CategoryController(val categoryRepository: CategoryRepository) extends FailFastCirceSupport {
  val routes: Route = pathPrefix("categories") {
    pathEndOrSingleSlash {
      get {
        complete {
          categoryRepository.all
        }
      } ~
        post {
          decodeRequest {
            entity(as[Category]) { category =>
              onSuccess(categoryRepository.findByTitle(category.title)) {
                case Some(_) => complete(StatusCodes.BadRequest)
                case None => complete(StatusCodes.Created, categoryRepository.create(category))
              }
            }
          }
        }
    } ~
      pathPrefix(Segment) { id =>
        pathEndOrSingleSlash {
          delete {
            onSuccess(categoryRepository.delete(id)) {
              case n if n > 0 => complete(StatusCodes.NoContent)
              case _ => complete(StatusCodes.NotFound)
            }
          }
        }
      }
  }

}
