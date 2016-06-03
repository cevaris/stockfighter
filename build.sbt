name := "stockfighter"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.7.4",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.7.3",
  "com.twitter" % "util-core_2.11" % "6.34.0",
  "javax.websocket" % "javax.websocket-api" % "1.1",
  "joda-time" % "joda-time" % "2.9.4",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.easymock" % "easymock" % "3.4",
  "org.glassfish.tyrus" % "tyrus-container-grizzly-client" % "1.12",
  "org.glassfish.tyrus.bundles" % "tyrus-standalone-client" % "1.12",
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.scalatest" % "scalatest_2.11" % "3.0.0-M15",
  "org.yaml" % "snakeyaml" % "1.17"
)