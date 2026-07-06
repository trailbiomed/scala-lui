import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.3.7"
ThisBuild / organization := "io.github.pityka"
ThisBuild / description  := "Typed UI component library for Laminar (Scala.js) with inline-only styling."
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

ThisBuild / dynverSeparator := "-"

ThisBuild / sonatypeCredentialHost := "central.sonatype.com"

ThisBuild / Compile / doc / sources := Seq.empty

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
  .aggregate(core, components, plot, pkce, example, devserver, e2e)
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

lazy val pkce = (project in file("pkce"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "pkce",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo"    %%% "laminar"     % "17.1.0",
      "org.scalameta" %%% "munit"      % "1.0.4" % Test
    ),
    scalaJSUseMainModuleInitializer := false
  )

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

// JVM end-to-end suite. Boots the dev server in-process, drives the example
// app through Playwright. Tests transitively require `example/fastLinkJS` so
// the bundle is up to date before any browser navigation. Not published.
lazy val e2e = (project in file("e2e"))
  .dependsOn(devserver)
  .settings(
    publish / skip := true,
    Test / fork := true,
    // Suites share a single Playwright Browser via E2EFixture; running test
    // classes concurrently can cause cross-test races (the dev server is
    // shared too). Force sequential execution.
    Test / parallelExecution := false,
    Test / baseDirectory := (LocalRootProject / baseDirectory).value,
    libraryDependencies ++= Seq(
      "com.microsoft.playwright" % "playwright" % "1.49.0" % Test,
      "org.scalameta"           %% "munit"      % "1.0.4"  % Test
    ),
    Test / test     := (Test / test).dependsOn(example / Compile / fastLinkJS).value,
    Test / testOnly := (Test / testOnly).dependsOn(example / Compile / fastLinkJS).evaluated
  )
