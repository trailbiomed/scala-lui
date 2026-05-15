package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Tooltip private[components] (
    val root: HtmlElement,
    private[components] val triggerSlot: HtmlElement
) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val placementVar: Var[Tooltip.Placement] = Var(Tooltip.Placement.Top)
}

/** Hover-only popover for short helper text. Wraps a trigger element; the tooltip appears
  * on pointer hover (no-op on touch input — `Interactive` gates `hovered` on mouse).
  *
  * {{{
  *   Tooltip(
  *     Tooltip.label := "Run analysis",
  *     Tooltip.placement := Tooltip.Placement.Top,
  *     Tooltip.trigger(IconButton(IconButton.icon := "▶", IconButton.ariaLabel := "Run"))
  *   )
  * }}}
  */
object Tooltip extends ComponentFactory[Tooltip] {

  enum Placement { case Top, Bottom, Left, Right }

  val label = Prop.in[String, Tooltip](_.labelVar)
  val placement = Prop.in[Placement, Tooltip](_.placementVar)

  /** Slot for the element that the tooltip describes. */
  def trigger(content: Modifier[HtmlElement]*): Mod[Tooltip] = el =>
    el.triggerSlot.amend(content*)

  override protected def build: Tooltip = {
    val triggerSlot = span()
    val bubble = span()
    val root = span(triggerSlot, bubble)
    val el = new Tooltip(root, triggerSlot)

    val hovered = Interactive.on(root).hovered

    root.amend(
      themed(_ =>
        css.position("relative") ++
          css.display(Display.InlineFlex) ++
          css.alignItems("center")
      )
    )

    triggerSlot.amend(
      themed(_ => css.display(Display.InlineFlex) ++ css.alignItems("center"))
    )

    bubble.amend(
      Signal.combine(hovered.signal, el.labelVar.signal, el.placementVar.signal).styled {
        case (t, (hov, lbl, pl)) =>
          val visible = hov && lbl.nonEmpty
          val (top, bottom, left, right, transform) = pl match {
            case Placement.Top =>
              ("auto", "100%", "50%", "auto", "translate(-50%, -6px)")
            case Placement.Bottom =>
              ("100%", "auto", "50%", "auto", "translate(-50%, 6px)")
            case Placement.Left =>
              ("50%", "auto", "auto", "100%", "translate(-6px, -50%)")
            case Placement.Right =>
              ("50%", "auto", "100%", "auto", "translate(6px, -50%)")
          }
          val (bg, fg) =
            if (t.isDark) (t.surface, t.text) else (palette.slate800, palette.white)
          css.position("absolute") ++
            css.raw("top", top) ++
            css.raw("bottom", bottom) ++
            css.raw("left", left) ++
            css.raw("right", right) ++
            css.raw("transform", transform) ++
            css.background(bg) ++
            css.color(fg) ++
            css.padding(Length.px(4), Length.px(8)) ++
            css.borderRadius(radius.sm) ++
            css.fontSize(fontSizes.md) ++
            css.fontWeight(FontWeight.Medium) ++
            css.raw("white-space", "nowrap") ++
            css.raw("pointer-events", "none") ++
            css.zIndex(50) ++
            css.opacity(if (visible) 1.0 else 0.0) ++
            css.transition("opacity", 120) ++
            css.raw("box-shadow", "0 4px 12px rgba(0,0,0,0.24)")
      },
      child.text <-- el.labelVar.signal
    )

    el
  }
}
