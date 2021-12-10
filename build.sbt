name := "nl-to-uml-demo"

version := "0.1"

scalaVersion := "2.12.8"
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.7"
val circeV = "0.12.0"
val enumeratumVersion = "1.5.13"
val enumeratumCirceVersion = "1.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-generic-extras" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  "io.circe" %% "circe-optics" % circeV,
  "io.circe" %% "circe-literal" % circeV,
  "io.circe" %% "circe-shapes" % circeV,
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumCirceVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % "4.3.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "4.3.2" classifier "models"
)

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
