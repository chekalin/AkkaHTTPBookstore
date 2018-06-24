package com.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.models.{User, UserJson}
import com.example.repository.UserRepository

class UserController(userRepository: UserRepository) extends UserJson {
  val routes: Route =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        post {
          decodeRequest {
            entity(as[User]) { user =>
              onSuccess(userRepository.findByEmail(user.email)) {
                case Some(_) => complete(StatusCodes.BadRequest)
                case None => complete(StatusCodes.Created, userRepository.create(user))
              }
            }
          }
        }
      } ~
        pathPrefix(IntNumber) { id =>
          get {
            onSuccess(userRepository.findById(id)) {
              case Some(user) => complete(user)
              case None => complete(StatusCodes.NotFound)
            }
          }
        }
    }

}
