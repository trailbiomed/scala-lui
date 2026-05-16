import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.3.7"
ThisBuild / organization := "io.github.pityka"
ThisBuild / homepage     := Some(url("https://github.com/trailbiomed/scala-lui"))
ThisBuild / licenses     := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / scmInfo      := Some(
  ScmInfo(
    url("https://github.com/trailbiomed/scala-lui"),
    "scm:git@github.com:trailbiomed/scala-lui.git"
  )
)
ThisBuild / developers   := List(
  Developer("pityka", "Istvan Bartha", "bartha.pityu@gmail.com", url("https://github.com/pityka"))
)

// Versioning: sbt-dynver derives the version from git tags.
// Tag like v0.1.0 -> 0.1.0; uncommitted -> 0.1.0+1-abcd1234-SNAPSHOT (etc).
ThisBuild / dynverSeparator := "-"

// Publish to GitHub Packages. Credentials come from CI env vars.
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := Some(
  "GitHub Package Registry" at "https://maven.pkg.github.com/trailbiomed/scala-lui"
)
ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  sys.env.getOrElse("GITHUB_ACTOR",   "x-access-token"),
  sys.env.getOrElse("GITHUB_TOKEN",   "")
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  // "-Wunused:all",
  "-Wvalue-discard",
  "-Xfatal-warnings",
  "-no-indent",
  "-encoding",
  "utf-8"
)

lazy val root = (project in file("."))
  .aggregate(core, components, plot, example, devserver)
  .settings(publish / skip := true)

lazy val core = (project in file("core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "lui-core",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo" %%% "laminar" % "17.1.0"
    )
  )

lazy val components = (project in file("components"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(name := "lui-components")

// Plot wrapper around nspl-canvas-js. Kept separate so apps that don't need
// charting don't pull in the renderer.
lazy val plot = (project in file("plot"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core, components)
  .settings(
    name := "lui-plot",
    libraryDependencies += "io.github.pityka" %%% "nspl-canvas-js" % "0.18.0",
    libraryDependencies += "io.github.pityka" %%% "nspl-svg-js" % "0.18.0",
  )

// Demo app. Not published.
lazy val example = (project in file("example"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(components, plot)
  .settings(
    publish / skip := true,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("example")))
    ),
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory :=
      baseDirectory.value / "public" / "scripts",
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory :=
      baseDirectory.value / "public" / "scripts"
  )

// JDK-only static dev server. Not published.
lazy val devserver = (project in file("devserver"))
  .settings(
    publish / skip := true,
    fork := true,
    Compile / run / baseDirectory := (LocalRootProject / baseDirectory).value
  )
