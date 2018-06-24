package com.example.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.example.models.{Auth, AuthJson, Credentials, CredentialsJson}
import com.example.repository.AuthRepository
import com.example.services.TokenService

class AuthController(authRepository: AuthRepository, tokenService: TokenService) extends CredentialsJson with AuthJson {
  val routes: Route = pathPrefix("auth") {
    pathEndOrSingleSlash {
      post {
        decodeRequest {
          entity(as[Credentials]) { credentials =>
            onSuccess(authRepository.findByCredentials(credentials)) {
              case Some(user) =>
                val token = tokenService.createToken(user)
                complete(Auth(user, token))
              case None => complete(StatusCodes.Unauthorized)
            }
          }
        }
      }
    }
  }
}
