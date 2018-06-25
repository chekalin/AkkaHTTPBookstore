package com.example.services

import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

trait DatabaseService {
  val driver: JdbcProfile
  val db: Database
}
