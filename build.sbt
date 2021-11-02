name := "Akka-Camp"

version := "0.1"

scalaVersion := "2.13.6"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.15"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.16" % Test

// https://mvnrepository.com/artifact/org.typelevel/discipline-scalatest
libraryDependencies += "org.typelevel" %% "discipline-scalatest" % "2.1.5" % Test
