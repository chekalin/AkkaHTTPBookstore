package com.example.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.example.models.User
import com.example.services.TokenService

import scala.concurrent.ExecutionContext

trait VerifyToken {

  val tokenService: TokenService
  implicit val ec: ExecutionContext

  def verifyToken: Directive1[User] = {
    headerValueByName("Authorization") flatMap { token =>
      onSuccess(tokenService.fetchUser(token)) flatMap {
        case Some(user) => provide(user)
        case None => complete(StatusCodes.Unauthorized)
      }
    }
  }

  def verifyTokenUser(userId: String): Directive1[User] = verifyToken flatMap { userInToken =>
    userInToken.id match {
      case Some(id) =>
        if (userId == id) provide(userInToken)
        else complete(StatusCodes.Unauthorized)
      case _ => complete(StatusCodes.Unauthorized)
    }
  }

}
