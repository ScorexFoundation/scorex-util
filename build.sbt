
name := "scorex-util"
description := "Common tools for scorex projects"

organization := "org.scorexfoundation"

lazy val scala213 = "2.13.8"
lazy val scala212 = "2.12.15"
lazy val scala211 = "2.11.12"

crossScalaVersions := Seq(scala212, scala211, scala213)
scalaVersion := scala212

javacOptions ++=
  "-source" :: "1.8" ::
    "-target" :: "1.8" ::
    Nil

resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Typesafe maven releases" at "https://repo.typesafe.com/typesafe/maven-releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "org.rudogma" %% "supertagged" % "1.5",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.+" % Test,
  // https://mvnrepository.com/artifact/org.scalatestplus/scalatestplus-scalacheck
   "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test

)

publishMavenStyle in ThisBuild := true
publishTo := sonatypePublishToBundle.value
publishArtifact in Test := true

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

fork in ThisBuild := true

pomIncludeRepository in ThisBuild := { _ => false }

licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode"))
homepage := Some(url("https://github.com/ScorexFoundation/scorex-util"))
pomExtra :=
  <developers>
    <developer>
      <id>kushti</id>
      <name>Alexander Chepurnoy</name>
      <url>http://chepurnoy.org/</url>
    </developer>
    <developer>
      <id>aslesarenko</id>
      <name>Alexander Slesarenko</name>
      <url>https://github.com/aslesarenko/</url>
    </developer>
  </developers>

// prefix version with "-SNAPSHOT" for builds without a git tag
dynverSonatypeSnapshots in ThisBuild := true
// use "-" instead of default "+"
dynverSeparator in ThisBuild := "-"


// PGP key for signing a release build published to sonatype
// signing is done by sbt-pgp plugin
// how to generate a key - https://central.sonatype.org/pages/working-with-pgp-signatures.html
// how to export a key see ci/import_gpg.sh
pgpPublicRing := file("ci/pubring.asc")
pgpSecretRing := file("ci/secring.asc")
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)
usePgpKeyHex("9D73AA38C08FD6AE5A51D3C11E4BF6F443599431")

