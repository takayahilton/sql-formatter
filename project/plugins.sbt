val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.3.0")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.0-M2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"                  % "3.9.4")
addSbtPlugin("com.github.gseitz"  % "sbt-release"                   % "1.0.13")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"                  % "2.4.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"                 % "1.6.1")
addSbtPlugin("org.wartremover"    % "sbt-wartremover"               % "2.4.10")
