name := "stockfighter"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

lazy val v = new {
  val http4s = "0.13.2"
  val jodaTime = "2.9.4"
  val scalaGuice = "4.0.1"
  val snakeYaml = "1.17"
}

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % v.jodaTime,
  "net.codingwell" %% "scala-guice" % v.scalaGuice,
  "org.http4s" %% "http4s-core" % v.http4s,
  "org.yaml" % "snakeyaml" % v.snakeYaml
)