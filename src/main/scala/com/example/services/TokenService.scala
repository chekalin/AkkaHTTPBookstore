package com.example.services

import com.example.models.{User, UserJson}
import com.example.repository.UserRepository
import pdi.jwt.{Jwt, JwtAlgorithm}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TokenService(userRepository: UserRepository)(implicit executionContext: ExecutionContext) extends UserJson {
  private val tempKey = "mySuperSecretKey"

  def createToken(user: User): String = {
    Jwt.encode(user.id.get.toJson.toString, tempKey, JwtAlgorithm.HS256)
  }

  def isTokenValid(token: String): Boolean = {
    Jwt.isValid(token, tempKey, Seq(JwtAlgorithm.HS256))
  }

  def fetchUser(token: String): Future[Option[User]] = {
    Jwt.decodeRaw(token, tempKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(json) =>
        val id = json.parseJson.convertTo[Long]
        userRepository.findById(id)
      case Failure(e) => Future.failed(e)
    }
  }

  def isTokenValidForMember(token: String, user: User): Future[Boolean] =
    fetchUser(token) map {
      case Some(fetchedUser) => user.id == fetchedUser.id
      case None => false
    }
}
