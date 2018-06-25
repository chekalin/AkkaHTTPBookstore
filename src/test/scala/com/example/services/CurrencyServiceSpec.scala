package com.example.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{AsyncWordSpec, MustMatchers}

class CurrencyServiceSpec extends AsyncWordSpec with MustMatchers {

  implicit val system: ActorSystem = ActorSystem("CurrencyServiceSpec")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  "CurrencyService" must {
    "return rates" in {
      CurrencyService.getRates map { rates =>
        rates mustBe defined
      }
    }
  }

}
