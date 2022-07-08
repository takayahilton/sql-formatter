val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.10.0")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.4")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"                  % "3.9.13")
addSbtPlugin("com.github.sbt"     % "sbt-release"                   % "1.0.15")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"                  % "2.4.6")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"                 % "1.8.2")
addSbtPlugin("org.wartremover"    % "sbt-wartremover"               % "2.4.16")
