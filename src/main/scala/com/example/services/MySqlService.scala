package com.example.services

import slick.jdbc.MySQLProfile.api._
import slick.jdbc.{JdbcProfile, MySQLProfile}

class MySqlService(jdbcUrl: String, dbUser: String, dbPassword: String) extends DatabaseService {

  val driver: JdbcProfile = MySQLProfile
  val db: Database = Database.forURL(jdbcUrl, dbUser, dbPassword)
  db.createSession()
}
