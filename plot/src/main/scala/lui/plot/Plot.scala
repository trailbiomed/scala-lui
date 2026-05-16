package lui.plot

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import org.nspl.{Build, CanvasRC, Identifier, Point, Renderable, Renderer, SvgRC}
import org.nspl.{canvasrenderer, svgrenderer}
import org.scalajs.dom

final class Plot[K <: Renderable[K]] private[plot] (
    val root: HtmlElement,
    private[plot] val buildVar: Var[Build[K]]
) extends Component {
  private[plot] val widthVar:           Var[Int]                             = Var(640)
  private[plot] val heightVar:          Var[Int]                             = Var(400)
  private[plot] val enableScrollVar:    Var[Boolean]                         = Var(true)
  private[plot] val enableDragVar:      Var[Boolean]                         = Var(true)
  private[plot] val enableCrosshairVar: Var[Boolean]                         = Var(false)
  private[plot] val plotClickBus:       EventBus[Identifier]                 = new EventBus
  private[plot] val hoverBus:           EventBus[Plot.PlotEvent]             = new EventBus
  private[plot] val unhoverBus:         EventBus[Plot.PlotEvent]             = new EventBus
  private[plot] val shapeClickBus:      EventBus[Plot.PlotEvent]             = new EventBus
  private[plot] val selectBus:          EventBus[collection.Seq[Identifier]] = new EventBus
}

/** A Laminar wrapper around an nspl plot. Two factories:
  *
  *   - `Plot.canvas(build)` — canvas backend. Smaller bundle (just `nspl-canvas-js`), fast at
  *     high point counts, no DOM nodes per point.
  *   - `Plot.svg(build)` — SVG backend. Renders to `<svg>`; suitable when you need
  *     vector output (CSS print, copy-paste into a vector tool), or per-shape DOM
  *     handles (e.g. CSS hover effects driven externally). Slower for big plots.
  *
  * Both share the same prop API — the only difference is which implicit renderer must
  * be in scope at the call site (`org.nspl.canvasrenderer.*` vs `org.nspl.svgrenderer.*`).
  *
  * The K type-parameter on `Plot[K]` is the nspl `Build` value type — in practice a long
  * compound type like `Elems3[XYPlotArea[…], TextBox, …]`. It's never typed out at the
  * call site; instead, users go through `Plot.canvas` / `Plot.svg`:
  *
  * {{{
  * import org.nspl.*
  * import org.nspl.canvasrenderer.*    // or org.nspl.svgrenderer.*
  * import lui.plot.*
  *
  * val data  = (0 to 100).map(i => (i.toDouble, math.sin(i * 0.2)))
  * val build = xyplot(data -> line())(par.xlab("i").ylab("sin"))
  *
  * val P = Plot.canvas(build)         // K inferred from `build`, captured in P
  * P(
  *   P.width  := 600,
  *   P.height := 250,
  *   P.click  --> clickObs,
  *   P.build  <-- buildSignal     // optional — re-renders on each emission
  * )
  * }}}
  *
  * Behavior:
  *   - The initial render uses the `initial` build captured by the factory.
  *   - `P.build <-- src` forwards emissions to nspl's in-place rAF updater, which
  *     preserves the user's accumulated zoom/pan via nspl's internal event store.
  *   - `width`, `height`, and the `enable*` flags are read once at construction. Binding a
  *     Signal to them works but only the initial value is honored — recreating the Plot
  *     is the only way to resize. */
object Plot {

  /** Bundle for hover / shape-click events. The `point` is in canvas / SVG-space (the
    * same coordinate space as `PlotAreaIdentifier.bounds`), suitable for feeding back
    * into `mouseToWorld`. */
  final case class PlotEvent(id: Identifier, point: Point, event: dom.MouseEvent)

  /** A K-bound bundle of typed props plus a factory. Each `Of[K]` is paired with one
    * `initial: Build[K]`; calling `apply(mods*)` materializes a `Plot[K]` whose first
    * paint shows `initial`. */
  trait Of[K <: Renderable[K]] {
    val build:           InOut[Build[K],                 Plot[K]]
    val width:           In[Int,                         Plot[K]]
    val height:          In[Int,                         Plot[K]]
    val enableScroll:    In[Boolean,                     Plot[K]]
    val enableDrag:      In[Boolean,                     Plot[K]]
    val enableCrosshair: In[Boolean,                     Plot[K]]
    val click:           Out[Identifier,                 Plot[K]]
    val hover:           Out[PlotEvent,                  Plot[K]]
    val unhover:         Out[PlotEvent,                  Plot[K]]
    val shapeClick:      Out[PlotEvent,                  Plot[K]]
    val select:          Out[collection.Seq[Identifier], Plot[K]]

    /** Create a `Plot[K]` from the captured `initial` build and the given mods. The
      * backing canvas / SVG element is created and the first paint is queued via
      * `requestAnimationFrame` before this returns. */
    def apply(mods: Mod[Plot[K]]*): Plot[K]
  }

  /** Canvas-backed factory. Requires `Renderer[K, CanvasRC]` in implicit scope —
    * `import org.nspl.canvasrenderer.*`. */
  def canvas[K <: Renderable[K]](initial: Build[K])(implicit
      r: Renderer[K, CanvasRC]
  ): Of[K] = bind(initial, canvasBackend[K])

  /** SVG-backed factory. Requires `Renderer[K, SvgRC]` in implicit scope —
    * `import org.nspl.svgrenderer.*`. */
  def svg[K <: Renderable[K]](initial: Build[K])(implicit
      r: Renderer[K, SvgRC]
  ): Of[K] = bind(initial, svgBackend[K])

  // --- internals -----------------------------------------------------------

  /** Backend-agnostic render signature: same parameters as the two nspl entrypoints,
    * unified return type at `dom.Element` (both `html.Canvas` and `SVGSVGElement`
    * widen). */
  private type RenderFn[K] = (
      Build[K],                                                      // initial
      Int, Int,                                                      // width, height
      Identifier => Unit,                                            // click
      Option[(Identifier, Point, dom.MouseEvent) => Unit],           // onHover
      Option[(Identifier, Point, dom.MouseEvent) => Unit],           // onUnhover
      Option[(Identifier, Point, dom.MouseEvent) => Unit],           // onShapeClick
      Option[collection.Seq[Identifier] => Unit],                    // onSelection
      Boolean, Boolean, Boolean                                      // scroll, drag, crosshair
  ) => (dom.Element, Build[K] => Unit)

  private def canvasBackend[K <: Renderable[K]](implicit r: Renderer[K, CanvasRC]): RenderFn[K] =
    (b, w, h, c, hv, uh, sc, se, es, ed, ec) =>
      canvasrenderer.render(b, w, h, c, hv, uh, sc, se, es, ed, ec)

  private def svgBackend[K <: Renderable[K]](implicit r: Renderer[K, SvgRC]): RenderFn[K] =
    (b, w, h, c, hv, uh, sc, se, es, ed, ec) =>
      svgrenderer.render(b, w, h, c, hv, uh, sc, se, es, ed, ec)

  private def bind[K <: Renderable[K]](initial: Build[K], render: RenderFn[K]): Of[K] = new Of[K] {
    val build           = Prop.inOut[Build[K],                 Plot[K]](_.buildVar)
    val width           = Prop.in[Int,                         Plot[K]](_.widthVar)
    val height          = Prop.in[Int,                         Plot[K]](_.heightVar)
    val enableScroll    = Prop.in[Boolean,                     Plot[K]](_.enableScrollVar)
    val enableDrag      = Prop.in[Boolean,                     Plot[K]](_.enableDragVar)
    val enableCrosshair = Prop.in[Boolean,                     Plot[K]](_.enableCrosshairVar)
    val click           = Prop.out[Identifier,                 Plot[K]](_.plotClickBus)
    val hover           = Prop.out[PlotEvent,                  Plot[K]](_.hoverBus)
    val unhover         = Prop.out[PlotEvent,                  Plot[K]](_.unhoverBus)
    val shapeClick      = Prop.out[PlotEvent,                  Plot[K]](_.shapeClickBus)
    val select          = Prop.out[collection.Seq[Identifier], Plot[K]](_.selectBus)

    def apply(mods: Mod[Plot[K]]*): Plot[K] = {
      val root = div()
      val el = new Plot[K](root, Var(initial))
      // Apply mods first so width / height / callbacks are set, and any `build <-- src`
      // has installed its writer subscription on `buildVar` (subscriptions only fire on
      // mount — see below for how we recover the up-to-date value at first paint).
      mods.foreach(_(el))

      val (backingEl, updater) = render(
        initial,
        el.widthVar.now(),
        el.heightVar.now(),
        (id: Identifier) => el.plotClickBus.writer.onNext(id),
        Some((id, p, e) => el.hoverBus.writer.onNext(PlotEvent(id, p, e))),
        Some((id, p, e) => el.unhoverBus.writer.onNext(PlotEvent(id, p, e))),
        Some((id, p, e) => el.shapeClickBus.writer.onNext(PlotEvent(id, p, e))),
        Some((ids: collection.Seq[Identifier]) => el.selectBus.writer.onNext(ids)),
        el.enableScrollVar.now(),
        el.enableDragVar.now(),
        el.enableCrosshairVar.now()
      )

      // signal --> Observer (not signal.changes): the initial mount-time emission is
      // `buildVar.now()`, which is `initial` for static plots or `src.now()` for streamed
      // ones — feeding it through the updater catches the "signal differs from initial"
      // case. nspl coalesces multiple update() calls within one rAF frame, so the
      // duplicate "initial" paint never reaches the screen.
      // `backingEl` is a raw `html.Canvas` or `SVGSVGElement` (not a Laminar element), so
      // we attach it via the parent DOM ref on first mount.
      val _ = root.amend(
        onMountCallback(ctx => {
          val _ = ctx.thisNode.ref.appendChild(backingEl)
        }),
        el.buildVar.signal --> Observer[Build[K]](updater)
      )
      el
    }
  }
}
