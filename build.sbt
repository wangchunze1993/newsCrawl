name := "arachne2"

version := "1.0"

scalaVersion := "2.11.7"

//akka
libraryDependencies ++= {
  val akkaVersion = "2.3.4"
  val nsalatimeVersion = "2.0.0"
  val jsoupVersion = "1.7.2"
  val httpclientVersion = "4.3.5"
  val logbackVersion = "1.1.2"
  val mysqlConnectorVersion = "5.1.31"
  val HikariCPVersion = "2.3.7"
  val slickVersion = "3.1.0"
  val thriftVersion = "0.9.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources(),
    "com.typesafe.akka" %% "akka-remote" % akkaVersion withSources(),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-osgi" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.github.nscala-time" % "nscala-time_2.11" % nsalatimeVersion,
    "org.jsoup" % "jsoup" % jsoupVersion,
    "org.apache.httpcomponents" % "httpclient" % httpclientVersion withSources(),
    "org.apache.httpcomponents" % "httpmime" % httpclientVersion withSources(),
    "org.apache.commons" % "commons-collections4" % "4.0",
    "commons-io" % "commons-io" % "2.4",
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "commons-codec" % "commons-codec" % "1.9",
    "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
    "com.zaxxer" % "HikariCP-java6" % HikariCPVersion,
    "com.typesafe.slick" %% "slick" % slickVersion withSources(),
    "com.typesafe.slick" %% "slick-codegen" % slickVersion,
    "net.liftweb" % "lift-json_2.11" % "3.0-M3"
  )
}

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


// If you need to specify main classes manually, use packSettings and packMain
packSettings

// [Optional] Creating `hello` command that calls org.mydomain.Hello#main(Array[String])
packMain := Map("arachne2" -> "com.neo.sk.arachne2.Main")

mainClass in assembly := Some("com.neo.sk.arachne2.Main")

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.first
  case PathList("play", "core", "server", xs@_*) => MergeStrategy.first
  case PathList("OSGI-OPT", xs@_*) => MergeStrategy.first
  case "logback.xml" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


