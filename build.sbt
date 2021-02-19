import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import ReleaseTransformations._

organization in ThisBuild := "com.github.takayahilton"

onChangedBuildSource in Global := ReloadOnSourceChanges

val Scala211 = "2.11.12"
val Scala212 = "2.12.12"
val Scala213 = "2.13.3"

lazy val root = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(publishingSettings)
  .settings(noPublishSettings)
  .aggregate(sql_formatterJVM, sql_formatterJS)

lazy val sql_formatter = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    moduleName := "sql-formatter",
    sharedSettings,
    publishingSettings,
    scalacOptions ++= commonScalacOptions.value,
    scalaVersion       := Scala211,
    crossScalaVersions := Seq(Scala211, Scala212, Scala213)
  )
  .jsSettings(
    //scalac-scoverage-plugin Scala.js 1.0 is not yet released.
    coverageEnabled := false
  )
  .nativeSettings(
    scalaVersion           := Scala211,
    crossScalaVersions     := Seq(Scala211),
    coverageEnabled        := false,
    Test / nativeLinkStubs := true,
    Compile / doc / scalacOptions -= "-Xfatal-warnings"
  )

lazy val sql_formatterJVM = sql_formatter.jvm
lazy val sql_formatterJS = sql_formatter.js
lazy val sql_formatterNative = sql_formatter.native

lazy val commonScalacOptions = Def.setting {
  Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:_",
    "-unchecked",
    "-Xlint",
    "-Xlint:-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  ) ++ {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        Seq(
          "-Ymacro-annotations"
        )
      case _ =>
        Seq(
          "-Xfatal-warnings", // doesn't work with SN, so removed in .nativeSettings (see: https://github.com/scala-native/scala-native/pull/1752)
          "-Yno-adapted-args",
          "-Ypartial-unification",
          "-Xfuture"
        )
    }
  }
}

wartremoverErrors in (Compile, compile) ++= Seq(
  Wart.ArrayEquals,
  Wart.AnyVal,
  Wart.DefaultArguments,
  Wart.Enumeration,
  Wart.ExplicitImplicitTypes,
  Wart.FinalCaseClass,
  Wart.FinalVal,
  Wart.LeakingSealed,
  Wart.NonUnitStatements,
  Wart.Serializable,
  Wart.TraversableOps
)

lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.2.5" % Test
  )
) ++ Seq(Compile, Test).map(scalacOptions in (_, console) -= "-Xfatal-warnings")

lazy val publishingSettings = Seq(
  name                    := "sql-formatter",
  description             := "SQL Formatter for Scala",
  publishMavenStyle       := true,
  publishArtifact in Test := false,
  pomIncludeRepository    := { _ => false },
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  homepage := Some(
    url("https://github.com/takayahilton/sql-formatter")
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/takayahilton/sql-formatter"),
      "scm:git@github.com:takayahilton/sql-formatter.git"
    )
  ),
  developers := List(
    Developer(
      id = "takayahilton",
      name = "Tanaka Takaya",
      email = "takayahilton@gmail.com",
      url = url("https://github.com/takayahilton")
    )
  )
) ++ sharedReleaseProcess

lazy val sharedReleaseProcess = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("check"),
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommandAndRemaining(s";++${Scala211}!;sql_formatterNative/publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val noPublishSettings = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false
)

addCommandAlias("check", ";scalafmtCheckAll;scalafmtSbtCheck")
addCommandAlias("fmt", ";scalafmtAll;scalafmtSbt")
