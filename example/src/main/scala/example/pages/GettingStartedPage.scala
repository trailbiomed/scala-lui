package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object GettingStartedPage {

  def apply(): HtmlElement = PageTemplate(
    title = "Getting started",
    summary = "A small project that renders a lui button. Three files, one shell command."
  )(
    PageTemplate.section("Project layout")(
      PageTemplate.paragraph(
        "lui is built with sbt. For small projects you can also use the `scala` command."
      ),
      PageTemplate.paragraph(
        "The setup below is roughly the minimum you need to compile to JS, mount onto a div, and render a component."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """my-app/
            |├─ index.html       <-- the page shell + script tag
            |├─ Main.scala       <-- the frontend
            |└─ build.sh         <-- a one-line compile script (optional)""".stripMargin
      )
    ),

    PageTemplate.section("index.html")(
      PageTemplate.paragraph(
        "You usually need a mount point (`<div id=\"app\">`) and a `<script type=\"module\">` pointing at the linker output. " 
      ),
      Code(
        Code.block := true,
        Code.text :=
          """<!doctype html>
            |<html lang="en">
            |  <head>
            |    <meta charset="utf-8" />
            |    <meta name="viewport"
            |          content="width=device-width, initial-scale=1" />
            |    <title>My app</title>
            |  </head>
            |  <body style="margin:0; font-family: ui-sans-serif, system-ui, sans-serif;">
            |    <div id="app"></div>
            |    <script type="module" src="./main.js"></script>
            |  </body>
            |</html>""".stripMargin
      )
    ),

    PageTemplate.section("Main.scala")(
      PageTemplate.paragraph(
        "This is the actual frontend application code, it wires the theme, mounts a Laminar root onto #app, and renders a button that flips dark/light mode."
      ),
      PageTemplate.paragraph(
        "Note the `Mod as _` alias and the namespaced `css.*` import. Both are project-wide conventions worth keeping."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """//> using scala 3.3.7
            |//> using platform scala-js
            |//> using jsModuleKind module
            |//> using dep io.github.pityka::lui-components::0.1.0
            |// (scalajs-dom and Laminar come in transitively via lui-components)
            |
            |import com.raquo.laminar.api.L.{Mod as _, *}
            |import org.scalajs.dom
            |import lui.*
            |import lui.components.*
            |import lui.style.*
            |
            |@main def main(): Unit = {
            |  val mount = dom.document.getElementById("app")
            |  Theme.signal.foreach { t =>
            |    dom.document.body.style.backgroundColor = t.bg.toCss
            |    dom.document.body.style.color = t.text.toCss
            |  }(unsafeWindowOwner)
            |
            |  val _ = render(mount, App())
            |}
            |
            |/** Your application code goes here. Add components, state, and routing
            |  * inside this function. The @main above it is mostly boilerplate. */
            |def App(): HtmlElement =
            |  div(
            |    css.padding(spacing.xxl) ++ stack.col(spacing.lg),
            |    Heading(1)("Hello, lui."),
            |    Text.muted("A small Laminar starter."),
            |    Button(
            |      Button.label <-- Theme.signal.map(t =>
            |        if (t.isDark) "☀ Switch to light" else "☾ Switch to dark"
            |      ),
            |      Button.click.foreach(_ => Theme.toggle())
            |    )
            |  )""".stripMargin
      )
    ),

    PageTemplate.section("Compile and run")(
      PageTemplate.paragraph(
        "The `scala` command reads the `//> using` directives at the top of Main.scala and pulls in the deps. " +
          "Packaging produces a single `main.js` next to your HTML."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """# one-time setup: install Scala 3.5+ (https://www.scala-lang.org/download/)
            |
            |# compile to main.js
            |scala --power package Main.scala --js -o main.js -f
            |
            |# serve the directory with any static server, e.g.
            |npx http-server . -p 8080      # node
            |# or:
            |caddy file-server --listen :8080
            |# or use the JDK-only devserver shipped in this repo (devserver/)
            |# then open http://localhost:8080""".stripMargin
      ),
      PageTemplate.paragraph(
        "For iterative work, `scala` can watch sources and re-link on every save:"
      ),
      Code(
        Code.block := true,
        Code.text := "scala --power package Main.scala --js -o main.js -f -w"
      )
    ),

    PageTemplate.section("From here")(
      Listing()(
        Listing.item(span(typo.body, "Browse the ", b("Components"), " section in the sidebar. Each page is a self-contained example.")),
        Listing.item(span(typo.body, "Read ", b("Foundations / Style & themed"), " for a tour of the inline-style model.")),
        Listing.item(span(typo.body, "See ", b("Foundations / Theme tokens"), " for how light/dark colors are organized."))
      )
    )
  )

  private def b(s: String): HtmlElement =
    span(themed(t => css.fontWeight(FontWeight.SemiBold) ++ css.color(t.text)), s)
}
