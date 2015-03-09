name := """charactor-clustered"""

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-testkit" % "2.4-SNAPSHOT" % "test",
   "com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
