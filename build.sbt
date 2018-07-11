
name := "scorex-util"

organization := "org.scorexfoundation"

version := "0.1.0"

scalaVersion := "2.12.6"

resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Typesafe maven releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.+",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.+",
  "ch.qos.logback" % "logback-classic" % "1.+",

  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

lazy val Benchmark = config("bench") extend Test

lazy val basic = Project("basic-with-separate-config", file("."))
  .settings(Defaults.coreDefaultSettings ++ Seq(
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.storm-enroute" %% "scalameter" % "0.10.1" % "bench" // ScalaMeter version is set in version.sbt
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Benchmark := false,
    logBuffered := false,
    javaOptions += "-Xmx4G"
  )
) configs(
  Benchmark
) settings(
  inConfig(Benchmark)(Defaults.testSettings): _*
)

//publishing settings

publishMavenStyle := true

publishArtifact in Test := false

fork := true

pomIncludeRepository := { _ => false }

//credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

//credentials ++= (for {
//  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
//  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
//} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

licenses in ThisBuild := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode"))

homepage in ThisBuild := Some(url("https://github.com/ScorexFoundation/scorex-util"))

pomExtra in ThisBuild :=
  <scm>
    <url>git@github.com/ScorexFoundation/scorex-util.git</url>
    <connection>scm:git@github.com/ScorexFoundation/scorex-util.git</connection>
  </scm>