enablePlugins(ScalaJSPlugin)

name := "sql-formatter demo"
scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "1.0.0",
  "com.github.takayahilton" %%% "sql-formatter" % "1.1.0"
)

scalaJSUseMainModuleInitializer := true

lazy val compileJs = taskKey[Unit]("Compile the project")
lazy val copyJsIndex = taskKey[Unit]("Copy Js to index.html")
lazy val copyJsMapIndex = taskKey[Unit]("Copy Js map to index.html")

copyJsIndex := {
  val from = target.value / "scala-2.13" / "sql-formatter-demo-opt.js"
  val to = baseDirectory.value / "sql-formatter-demo-opt.js"
  IO.copyFile(from, to)
}

copyJsMapIndex := {
  val from = target.value / "scala-2.13" / "sql-formatter-demo-opt.js.map"
  val to = baseDirectory.value / "sql-formatter-demo-opt.js.map"
  IO.copyFile(from, to)
}

compileJs := Def.sequential(
  fullOptJS in Compile,
  copyJsMapIndex,
  copyJsIndex
).value