package com.example.services

import slick.driver.PostgresDriver.api._
import slick.driver.{JdbcProfile, PostgresDriver}

class PostgresService(jdbcUrl: String, dbUser: String, dbPassword: String) extends DatabaseService {

  val driver: JdbcProfile = PostgresDriver
  val db: Database = Database.forURL(jdbcUrl, dbUser, dbPassword)
  db.createSession()
}
