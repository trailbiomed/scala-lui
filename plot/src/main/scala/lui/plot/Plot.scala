package lui.plot

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import org.nspl.{Build, CanvasRC, Identifier, Point, Renderable, Renderer}
import org.nspl.canvasrenderer
import org.scalajs.dom

final class Plot[K <: Renderable[K]] private[plot] (
    val root: HtmlElement,
    private[plot] val buildVar: Var[Build[K]]
) extends Component {
  private[plot] val widthVar:           Var[Int]                   = Var(640)
  private[plot] val heightVar:          Var[Int]                   = Var(400)
  private[plot] val enableScrollVar:    Var[Boolean]               = Var(true)
  private[plot] val enableDragVar:      Var[Boolean]               = Var(true)
  private[plot] val enableCrosshairVar: Var[Boolean]               = Var(false)
  private[plot] val plotClickBus:       EventBus[Identifier]       = new EventBus
  private[plot] val hoverBus:           EventBus[Plot.PlotEvent]   = new EventBus
  private[plot] val unhoverBus:         EventBus[Plot.PlotEvent]   = new EventBus
  private[plot] val shapeClickBus:      EventBus[Plot.PlotEvent]   = new EventBus
  private[plot] val selectBus:          EventBus[collection.Seq[Identifier]] = new EventBus
}

/** A Laminar wrapper around an nspl canvas plot.
  *
  * The K type-parameter on `Plot[K]` is the nspl `Build` value type — in practice a long
  * compound type like `Elems3[XYPlotArea[…], TextBox, …]`. It's never typed out at the
  * call site; instead, users go through [[Plot.of]]:
  *
  * {{{
  * import org.nspl.*
  * import org.nspl.canvasrenderer.*    // brings the implicit Renderer[K, CanvasRC]
  * import lui.plot.*
  *
  * val data  = (0 to 100).map(i => (i.toDouble, math.sin(i * 0.2)))
  * val build = xyplot(data -> line())(par.xlab("i").ylab("sin"))
  *
  * val P = Plot.of(build)         // K inferred from `build`, captured in P
  * P(
  *   P.width  := 600,
  *   P.height := 250,
  *   P.click  --> clickObs,
  *   P.build  <-- buildSignal     // optional — re-renders on each emission
  * )
  * }}}
  *
  * Behavior:
  *   - The initial render uses the `initial` build captured by `Plot.of`.
  *   - `P.build <-- src` forwards `src`'s emissions to nspl's in-place rAF updater, which
  *     preserves the user's accumulated zoom/pan via nspl's internal event store.
  *   - `width`, `height`, and the `enable*` flags are read once at construction. Binding a
  *     Signal to them works but only the current value is honored — re-creating the Plot
  *     is the only way to resize.
  *
  * @see [[org.nspl.canvasrenderer.render]] — the underlying nspl entrypoint. */
object Plot {

  /** Bundle for hover / shape-click events. The `point` is in canvas-space (the same
    * coordinate space as `PlotAreaIdentifier.bounds`), suitable for feeding back into
    * `mouseToWorld`. */
  final case class PlotEvent(id: Identifier, point: Point, event: dom.MouseEvent)

  /** A K-bound bundle of typed props plus a factory. Created via [[Plot.of]]. Each `Of[K]`
    * is paired with one `initial: Build[K]`; calling `apply(mods*)` materializes a
    * `Plot[K]` whose first paint shows `initial`. */
  trait Of[K <: Renderable[K]] {
    val build:           InOut[Build[K],      Plot[K]]
    val width:           In[Int,              Plot[K]]
    val height:          In[Int,              Plot[K]]
    val enableScroll:    In[Boolean,          Plot[K]]
    val enableDrag:      In[Boolean,          Plot[K]]
    val enableCrosshair: In[Boolean,          Plot[K]]
    val click:           Out[Identifier,      Plot[K]]
    val hover:           Out[PlotEvent,       Plot[K]]
    val unhover:         Out[PlotEvent,       Plot[K]]
    val shapeClick:      Out[PlotEvent,       Plot[K]]
    val select:          Out[collection.Seq[Identifier], Plot[K]]

    /** Create a `Plot[K]` from the captured `initial` build and the given mods. The canvas
      * is created and the first paint is queued via `requestAnimationFrame` before this
      * returns. */
    def apply(mods: Mod[Plot[K]]*): Plot[K]
  }

  /** Capture `K` from `initial` and the implicit `Renderer[K, CanvasRC]` at the call site.
    * The returned bundle threads the same `K` through all of its props and the eventual
    * `Plot[K]`, so no type annotation is needed at the use site. */
  def of[K <: Renderable[K]](initial: Build[K])(implicit
      r: Renderer[K, CanvasRC]
  ): Of[K] = new Of[K] {
    val build           = Prop.inOut[Build[K],      Plot[K]](_.buildVar)
    val width           = Prop.in[Int,              Plot[K]](_.widthVar)
    val height          = Prop.in[Int,              Plot[K]](_.heightVar)
    val enableScroll    = Prop.in[Boolean,          Plot[K]](_.enableScrollVar)
    val enableDrag      = Prop.in[Boolean,          Plot[K]](_.enableDragVar)
    val enableCrosshair = Prop.in[Boolean,          Plot[K]](_.enableCrosshairVar)
    val click           = Prop.out[Identifier,      Plot[K]](_.plotClickBus)
    val hover           = Prop.out[PlotEvent,       Plot[K]](_.hoverBus)
    val unhover         = Prop.out[PlotEvent,       Plot[K]](_.unhoverBus)
    val shapeClick      = Prop.out[PlotEvent,       Plot[K]](_.shapeClickBus)
    val select          = Prop.out[collection.Seq[Identifier], Plot[K]](_.selectBus)

    def apply(mods: Mod[Plot[K]]*): Plot[K] = {
      val root = div()
      val el = new Plot[K](root, Var(initial))
      // Apply mods first so width / height / callbacks are set, and any `build <-- src`
      // has installed its writer subscription on `buildVar` (subscriptions only fire on
      // mount — see below for how we recover the up-to-date value at first paint).
      mods.foreach(_(el))

      val (canvas, updater) = canvasrenderer.render(
        initial,
        el.widthVar.now(),
        el.heightVar.now(),
        click = id => el.plotClickBus.writer.onNext(id),
        onHover = Some((id, p, e) => el.hoverBus.writer.onNext(PlotEvent(id, p, e))),
        onUnhover = Some((id, p, e) => el.unhoverBus.writer.onNext(PlotEvent(id, p, e))),
        onShapeClick = Some((id, p, e) => el.shapeClickBus.writer.onNext(PlotEvent(id, p, e))),
        onSelection = Some(ids => el.selectBus.writer.onNext(ids)),
        enableScroll = el.enableScrollVar.now(),
        enableDrag = el.enableDragVar.now(),
        enableCrosshair = el.enableCrosshairVar.now()
      )(r)

      // signal --> Observer (not signal.changes): the initial mount-time emission is
      // `buildVar.now()`, which is `initial` for static plots or `src.now()` for streamed
      // ones — feeding it through the updater catches the "signal differs from initial"
      // case. nspl coalesces multiple update() calls within one rAF frame, so the
      // duplicate "initial" paint never reaches the screen.
      // `canvas` is a raw HTMLCanvasElement (not a Laminar ReactiveHtmlElement), so we
      // attach it via the parent DOM ref. Doing it on first mount means it survives
      // across the parent's binder activations and Laminar still tears it down when
      // the parent is detached.
      val _ = root.amend(
        onMountCallback(ctx => {
          val _ = ctx.thisNode.ref.appendChild(canvas)
        }),
        el.buildVar.signal --> Observer[Build[K]](updater)
      )
      el
    }
  }
}
