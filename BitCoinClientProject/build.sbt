name := "HelloRemote"

version := "1.0"

scalaVersion := "2.11.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.2",
    "com.typesafe.akka" %% "akka-remote" % "2.3.2"
)
