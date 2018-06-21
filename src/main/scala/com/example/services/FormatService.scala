package com.example.services

import java.sql.Date

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

object FormatService {
  implicit object DateJsonFromat extends RootJsonFormat[Date] {
    override def write(date: Date): JsValue = JsString(date.toString)

    override def read(value: JsValue): Date = value match {
      case JsString(dateStr) => Date.valueOf(dateStr)
      case _ => throw DeserializationException("Date expected")
    }
  }

}
