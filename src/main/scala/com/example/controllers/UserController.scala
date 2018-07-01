package com.example.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.directives.VerifyToken
import com.example.models.User
import com.example.repository.UserRepository
import com.example.services.TokenService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class UserController(val userRepository: UserRepository, val tokenService: TokenService)(implicit val ec: ExecutionContext) extends FailFastCirceSupport with VerifyToken {
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
        pathPrefix(Segment) { id =>
          pathEndOrSingleSlash {
            verifyTokenUser(id) { user =>
              get {
                complete(user)
              }
            }
          }
        }
    }

}
