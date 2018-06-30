package com.example.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser._
import io.circe.{Decoder, Encoder}

import scala.concurrent.{ExecutionContext, Future}

object CurrencyService {

  val logger = Logger("CurrencyService")

  private case class FrankfurterResponse(rates: Map[String, Float])

  private object FrankfurterResponse {
    implicit val frankfurterResponseEncoder: Encoder[FrankfurterResponse] = deriveEncoder[FrankfurterResponse]
    implicit val frankfurterResponseDecoder: Decoder[FrankfurterResponse] = deriveDecoder[FrankfurterResponse]
  }

  val baseCurrency = "USD"
  val supportedCurrencies = Seq("USD", "EUR")

  private val exchangeCurrencies = supportedCurrencies.filterNot(_ == baseCurrency)

  private val requestUrl = s"https://frankfurter.app/current?from=$baseCurrency&to=${exchangeCurrencies.mkString(",")}"

  private val request = HttpRequest(HttpMethods.GET, requestUrl)

  def getRates(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer): Future[Option[Map[String, Float]]] =
    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK if response.entity.contentType == ContentTypes.`application/json` =>
          Unmarshal(response.entity).to[String].map { jsonString =>
            decode[FrankfurterResponse](jsonString) match {
              case Right(FrankfurterResponse(rates)) =>
                if (rates.isEmpty) None
                else Some(rates)
              case Left(failure) =>
                logger.error(s"cannot parse response from FrankfurterApp, $failure")
                None
            }
          }
        case _ => Future.successful(None)
      }
    }

}
