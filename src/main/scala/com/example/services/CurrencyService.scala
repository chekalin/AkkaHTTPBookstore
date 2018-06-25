package com.example.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContext, Future}

object CurrencyService {

  private case class FrankfurterResponse(rates: Map[String, Float])

  private object FrankfurterResponseJson extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val frankfurterResponseJsonFormat: RootJsonFormat[FrankfurterResponse] = jsonFormat1(FrankfurterResponse.apply)
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
          import FrankfurterResponseJson._
          import spray.json._
          Unmarshal(response.entity).to[String].map { jsonString =>
            val rates = jsonString.parseJson.convertTo[FrankfurterResponse].rates
            if (rates.isEmpty) None
            else Some(rates)
          }
        case _ => Future.successful(None)
      }
    }

}
