name := """charactor-clustered"""

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

val akkaVer = "2.3.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVer,
  "com.typesafe.akka" %% "akka-actor" % akkaVer,
  "com.typesafe.akka" %% "akka-testkit" % akkaVer % "test",
   "com.typesafe.akka" %% "akka-cluster" % akkaVer,
   "akka-raft" %% "akka-raft" % "0.1-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

scalacOptions ++= Seq(
  "-Xlint" ,
  "-deprecation" ,
  "-Xfatal-warnings"
  )