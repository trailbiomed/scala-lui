package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*
import lui.plot.*
import org.nspl.*
import org.nspl.canvasrenderer.*

object PlotPages {

  // ---------------------------------------------------------------------------
  def plot(): HtmlElement = PageTemplate(
    title = "Plot",
    summary = "Wraps an nspl-canvas plot. Lives in the lui-plot subproject so apps that " +
      "don't chart don't pay for the renderer."
  )(

    PageTemplate.section("Module")(
      PageTemplate.paragraph(
        "Plot lives in lui.plot (sbt module lui-plot). It depends on io.github.pityka::nspl-canvas-js, " +
          "and that dep is the reason it's separate from lui-components."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// build.sbt
            |libraryDependencies += "io.github.pityka" %%% "lui-plot" % "<version>"
            |
            |// at the call site:
            |import org.nspl.*
            |import org.nspl.canvasrenderer.*       // brings the implicit Renderer[K, CanvasRC]
            |import lui.plot.*""".stripMargin
      )
    ),

    PageTemplate.section("Why Plot.of(initial)")(
      PageTemplate.paragraph(
        "nspl plot types are long compound types like Elems3[XYPlotArea[...], TextBox, ...]. You don't " +
          "want to write them out. `Plot.of(build)` captures the K via type inference and returns a bundle " +
          "of typed props paired with a factory — the K stays hidden inside the bundle."
      )
    ),

    // -------------------------------------------------------------------------
    PageTemplate.section("Static line plot")(
      PageTemplate.codedDemo(
        "Plot.of(build)(...)",
        """val data  = (0 to 100).map(i => (i.toDouble, math.sin(i * 0.2)))
          |val build = xyplot(data -> line())(par.xlab("i").ylab("sin"))
          |val P = Plot.of(build)
          |P(P.width := 600, P.height := 240)""".stripMargin
      )({
        val data = (0 to 100).map(i => (i.toDouble, math.sin(i * 0.2)))
        val build = xyplot(data -> line())(par.xlab("i").ylab("sin"))
        val P = Plot.of(build)
        P(P.width := 600, P.height := 240)
      })
    ),

    // -------------------------------------------------------------------------
    PageTemplate.section("Hover + click callbacks")(
      PageTemplate.paragraph(
        "Identifiers are an open trait — toString on them is opaque, so pattern-match on the " +
          "subtype to extract what you actually care about. For data points carrying " +
          "`noIdentifier = false`, the rowIdx field of `DataRowIdx` is the index into the " +
          "originally-supplied data sequence."
      ),
      PageTemplate.codedDemo(
        "binding-style + structured Identifier",
        """val msg = Var("Hover over a point…")
          |val data = (1 to 60).map { i =>
          |  val rng = new scala.util.Random(i)
          |  (rng.nextGaussian(), rng.nextGaussian())
          |}
          |val build = xyplot(data -> point(size = 6d, noIdentifier = false))(par.xlab("x").ylab("y"))
          |val P = Plot.of(build)
          |
          |def describe(id: Identifier): String = id match {
          |  case DataRowIdx(_, _, row) =>
          |    data.lift(row) match {
          |      case Some((x, y)) => f"row $row at ($x%.2f, $y%.2f)"
          |      case None         => s"row $row"
          |    }
          |  case _: PlotAreaIdentifier       => "plot area"
          |  case TextBoxIdentifier(label, _) => s"label \"$label\""
          |  case EmptyIdentifier             => "—"
          |  case other                       => other.toString
          |}
          |
          |div(
          |  P(
          |    P.width      := 600,
          |    P.height     := 260,
          |    P.hover.map(e => s"hover ${describe(e.id)}")       --> msg.writer,
          |    P.unhover.mapTo("…")                                --> msg.writer,
          |    P.shapeClick.map(e => s"click ${describe(e.id)}")  --> msg.writer
          |  ),
          |  span(typo.hint, child.text <-- msg.signal)
          |)""".stripMargin
      )({
        val msg = Var("Hover over a point…")
        val data = (1 to 60).map { i =>
          val rng = new scala.util.Random(i)
          (rng.nextGaussian(), rng.nextGaussian())
        }
        val build = xyplot(data -> point(size = 6d, noIdentifier = false))(par.xlab("x").ylab("y"))
        val P = Plot.of(build)

        def describe(id: Identifier): String = id match {
          case DataRowIdx(_, _, row) =>
            data.lift(row) match {
              case Some((x, y)) => f"row $row at ($x%.2f, $y%.2f)"
              case None         => s"row $row"
            }
          case _: PlotAreaIdentifier       => "plot area"
          case TextBoxIdentifier(label, _) => s"label \"$label\""
          case EmptyIdentifier             => "—"
          case other                       => other.toString
        }

        div(
          stack.col(spacing.sm),
          P(
            P.width      := 600,
            P.height     := 260,
            P.hover.map(e => s"hover ${describe(e.id)}")       --> msg.writer,
            P.unhover.mapTo("…")                                --> msg.writer,
            P.shapeClick.map(e => s"click ${describe(e.id)}")  --> msg.writer
          ),
          span(typo.hint, child.text <-- msg.signal)
        )
      })
    ),

    // -------------------------------------------------------------------------
    PageTemplate.section("Streamed updates")(
      PageTemplate.paragraph(
        "Bind a `Signal[Build[K]]` via `P.build <-- src` to push new data into the same canvas. nspl's " +
          "in-place rAF updater preserves the user's accumulated zoom/pan via its event store, so the " +
          "panel stays where the user left it across data swaps."
      ),
      PageTemplate.codedDemo(
        "P.build <-- buildSignal",
        """val freq = Var(0.2)
          |def buildFor(f: Double) = {
          |  val data = (0 to 100).map(i => (i.toDouble, math.sin(i * f)))
          |  xyplot(data -> line())(par.xlab("i").ylab("sin"))
          |}
          |val buildSignal = freq.signal.map(buildFor)
          |val P = Plot.of(buildFor(freq.now()))
          |
          |div(
          |  Slider(Slider.value <--> freq, Slider.min := 0.05, Slider.max := 0.5, Slider.step := 0.01),
          |  P(P.width := 600, P.height := 240, P.build <-- buildSignal)
          |)""".stripMargin
      )({
        val freq = Var(0.2)
        def buildFor(f: Double) = {
          val data = (0 to 100).map(i => (i.toDouble, math.sin(i * f)))
          xyplot(data -> line())(par.xlab("i").ylab("sin"))
        }
        val buildSignal = freq.signal.map(buildFor)
        val P = Plot.of(buildFor(freq.now()))
        div(
          stack.col(spacing.md) ++ css.alignItems("flex-start"),
          div(
            stack.row(spacing.md) ++ css.alignItems("center"),
            span(typo.hint, "frequency"),
            Slider(Slider.value <--> freq, Slider.min := 0.05, Slider.max := 0.5, Slider.step := 0.01, Slider.width := Length.px(220)),
            span(typo.hint, child.text <-- freq.signal.map(f => f"$f%.2f"))
          ),
          P(P.width := 600, P.height := 240, P.build <-- buildSignal)
        )
      })
    ),

    PageTemplate.behavior(
      "Plot.of(initial) captures K by type inference; the bundle's apply(...) creates the canvas and queues the first paint via requestAnimationFrame.",
      "Width and height are read once at construction. Binding a Signal to them works, but only the initial value is honored — resizing requires recreating the plot.",
      "Callbacks (click, hover, unhover, shapeClick, select) fire at the browser's mousemove / mouseup rate, coalesced to one event per rAF frame. Keep observers cheap.",
      "P.build <-- src feeds new builds into nspl's rAF updater. Multiple updates within one frame coalesce."
    ),

    PageTemplate.propsTable(
      ("build",           "InOut[Build[K]]",                 "Stream of plot data. Initial value comes from Plot.of(initial)."),
      ("width",           "In[Int]",                         "Canvas width. Read once at construction."),
      ("height",          "In[Int]",                         "Canvas height. Read once at construction."),
      ("enableScroll",    "In[Boolean]",                     "Wheel-to-zoom on the plot area."),
      ("enableDrag",      "In[Boolean]",                     "Plain drag pans the plot."),
      ("enableCrosshair", "In[Boolean]",                     "Render an nspl crosshair overlay on hover."),
      ("click",           "Out[Identifier]",                 "Plot-area mousedown. Identifies the area, not a shape."),
      ("hover",           "Out[PlotEvent]",                  "Cursor over a hover-enabled shape or plot area."),
      ("unhover",         "Out[PlotEvent]",                  "Cursor leaves a previously-hovered shape / area."),
      ("shapeClick",      "Out[PlotEvent]",                  "Click (no drag) on a shape with a non-empty Identifier."),
      ("select",          "Out[collection.Seq[Identifier]]", "Identifiers whose center landed in a shift+drag rectangle.")
    )
  )
}
