name := "next-word-prediction"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.apache.spark" % "spark-core_2.11" % "2.0.0",
  "org.apache.spark" % "spark-sql_2.11" % "2.0.0",
  "org.apache.spark" % "spark-mllib_2.11" % "2.0.0"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.2" force(),
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.2" force(),
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.2" force()
)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
