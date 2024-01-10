
lazy val scalac: Seq[String] = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
//  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  // "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-numeric-widen"              // Warn when numerics are widened.
  //"-Xlog-free-terms",
)

lazy val scalac211: Seq[String] = Seq(
  "-optimize",
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Xfuture",                          // Turn on future language features.
)

lazy val scalac212: Seq[String] = Seq(
  "-opt:simplify-jumps",
  "-opt:compact-locals",
  "-opt:copy-propagation",
  "-opt:box-unbox",
  "-opt:closure-invocations",
  "-opt:unreachable-code",
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xfuture",                          // Turn on future language features.
)

lazy val scalac213: Seq[String] = Seq(
  "-opt:simplify-jumps",
  "-opt:compact-locals",
  "-opt:copy-propagation",
  "-opt:box-unbox",
  "-opt:closure-invocations",
  "-opt:unreachable-code",
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
)

lazy val scalac3: Seq[String] = Seq(
  "-source:3.0-migration"
)

lazy val scala213 = "2.13.11"
lazy val scala212 = "2.12.18"
lazy val scala211 = "2.11.12"
lazy val scala3   = "3.3.1"

crossScalaVersions := Seq(scala211, scala212, scala213, scala3)
scalaVersion := scala213
organization := "org.scorexfoundation"

javacOptions ++=
  "-source" :: "1.8" ::
    "-target" :: "1.8" ::
    Nil

lazy val utilSettings = Seq(
  organization := "org.scorexfoundation",
  licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0/legalcode")),
  homepage := Some(url("http://github.com/ScorexFoundation/scorex-util")),
  description := "Common tools for scorex projects",

  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  libraryDependencies ++= Seq(
    ("org.rudogma" %%% "supertagged" % "2.0-RC2").cross(CrossVersion.for3Use2_13),
  ) ++ {
    if (scalaVersion.value == scala3)
      Seq(
        "org.scalatest" %%% "scalatest" % "3.3.0-alpha.1" % Test,
        "org.scalatest" %%% "scalatest-propspec" % "3.3.0-alpha.1" % Test,
        "org.scalatest" %%% "scalatest-shouldmatchers" % "3.3.0-alpha.1" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.15.3" % Test,
        "org.scalatestplus" %%% "scalacheck-1-17" % "3.3.0.0-alpha.1" % Test
      )
    else // use last versions with Scala 2.11 support
      Seq(
        "org.scalatest" %%% "scalatest" % "3.3.0-SNAP3" % Test,
        "org.scalatest" %%% "scalatest-propspec" % "3.3.0-SNAP3" % Test,
        "org.scalatest" %%% "scalatest-shouldmatchers" % "3.3.0-SNAP3" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.15.2" % Test,
        "org.scalatestplus" %%% "scalacheck-1-15" % "3.3.0.0-SNAP3" % Test
      )
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n == 13 =>
        scalac ++ scalac213
      case Some((2, n)) if n == 12 =>
        scalac ++ scalac212
      case Some((2, 11)) =>
        scalac ++ scalac211
      case Some((3, _)) =>
        scalac ++ scalac3
    }
  },
  javacOptions ++= javacReleaseOption,
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value,
  pomExtra := (
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
      ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/ScorexFoundation/scorex-util"),
      "scm:git@github.com:ScorexFoundation/scorex-util.git"
    )
  )
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
    .in(file("."))
    .settings(moduleName := "scorex-util")
    .jvmSettings(
      scalaVersion := scala213,
      crossScalaVersions := Seq(scala211, scala212, scala213, scala3),
      libraryDependencies ++= Seq(
        "com.typesafe.scala-logging" %%% "scala-logging" % "3.9.5"
      ),
    )
    .settings(utilSettings)
    .jsSettings(
      scalaVersion := scala213,
      crossScalaVersions := Seq(scala3, scala213),
      libraryDependencies ++= Seq(
      ),
      Test / parallelExecution := false
    )

def javacReleaseOption = {
  if (System.getProperty("java.version").startsWith("1."))
  // java <9 "--release" is not supported
    Seq()
  else
    Seq("--release", "8")
}

// prefix version with "-SNAPSHOT" for builds without a git tag
ThisBuild / dynverSonatypeSnapshots := true
// use "-" instead of default "+"
ThisBuild / dynverSeparator := "-"

Test / publishArtifact := true
pomIncludeRepository := { _ => false }

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq


// PGP key for signing a release build published to sonatype
// signing is done by sbt-pgp plugin
// how to generate a key - https://central.sonatype.org/pages/working-with-pgp-signatures.html
// how to export a key see ci/import_gpg.sh
pgpPublicRing := file("ci/pubring.asc")
pgpSecretRing := file("ci/secring.asc")
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toArray)
usePgpKeyHex("9D73AA38C08FD6AE5A51D3C11E4BF6F443599431")

