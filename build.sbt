name := "AkkaHTTPBookstore"

version := "0.1"

scalaVersion := "2.12.6"

val akkaHttpVersion = "10.1.3"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.pauldijou" %% "jwt-core" % "0.16.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.flywaydb" % "flyway-core" % "3.2.1",
  "com.github.t3hnar" %% "scala-bcrypt" % "3.0"
)

parallelExecution in Test := false

enablePlugins(JavaAppPackaging)