package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class FullscreenOverlay private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val zIndexVar: Var[Int] = Var(100)
  private[components] val trapFocusVar: Var[Boolean] = Var(true)
  private[components] val closeBus: EventBus[Unit] = new EventBus[Unit]

  /** Fires when the overlay wants to close — Escape, or any external sink wired to
    * `FullscreenOverlay.close`. Consumers can gate their own dismiss behavior on this. */
  val closes: EventStream[Unit] = closeBus.events
}

/** A viewport-filling overlay (position: fixed covering top/right/bottom/left).
  * Use for slideshows, presentation views, kiosk-mode surfaces, or any "take
  * over the whole screen" affordance. Unlike `Modal` there's no backdrop and
  * no visible chrome — the body slot owns the entire viewport.
  *
  * Behavior when open:
  *  - Escape closes (unless caller intercepts via `.closes`).
  *  - Focus moves into the overlay on open and restores on close.
  *  - Optional focus-trap (`trapFocus`, default true) cycles Tab within the overlay.
  *
  * {{{
  *   val open = Var(false)
  *   FullscreenOverlay(
  *     FullscreenOverlay.open <--> open,
  *     FullscreenOverlay.body(
  *       div(
  *         stack.grow ++ css.padding(spacing.xxl),
  *         Heading(1)("Now presenting"),
  *         // …slide content…
  *       )
  *     )
  *   )
  * }}}
  */
object FullscreenOverlay extends ComponentFactory[FullscreenOverlay] {

  val open = Prop.inOut[Boolean, FullscreenOverlay](_.openVar)

  /** Stacking order. Default `100` — above sticky headers/footers, below toast at `200`. */
  val zIndex = Prop.in[Int, FullscreenOverlay](_.zIndexVar)

  /** When true (default), Tab and Shift+Tab cycle focus within the overlay. */
  val trapFocus = Prop.in[Boolean, FullscreenOverlay](_.trapFocusVar)

  /** Sink for external dismiss actions (e.g. an "Exit" button). Equivalent to
    * setting `open := false`, but composes as an event sink for `-->`. */
  val close = Prop.out[Unit, FullscreenOverlay](_.closeBus)

  /** Content slot filling the overlay. */
  def body(content: Modifier[HtmlElement]*): Mod[FullscreenOverlay] = el =>
    el.bodySlot.amend(content*)

  override protected def build: FullscreenOverlay = {
    val bodySlot = div()
    val root = div(bodySlot)
    val el = new FullscreenOverlay(root, bodySlot)

    root.amend(
      Signal.combine(el.openVar.signal, el.zIndexVar.signal).styled { case (t, (isOpen, z)) =>
        css.position("fixed") ++
          css.top(Length.zero) ++
          css.raw("right", "0") ++
          css.raw("bottom", "0") ++
          css.left(Length.zero) ++
          css.zIndex(z) ++
          css.background(t.bg) ++
          css.color(t.text) ++
          css.display(if (isOpen) Display.Flex else Display.None) ++
          css.flexDirection("column")
      },
      role := "dialog",
      AriaExtras.ariaModal := "true",
      tabIndex := -1,
      el.closeBus.events.mapTo(false) --> el.openVar.writer
    )

    bodySlot.amend(
      stack.col(Length.zero) ++ stack.grow ++ css.raw("min-height", "0") ++ css.raw("min-width", "0")
    )

    Overlay.install(
      containerEl = root,
      openSignal  = el.openVar.signal,
      close       = () => el.closeBus.writer.onNext(()),
      trapFocus   = el.trapFocusVar.now()
    )

    el
  }
}
