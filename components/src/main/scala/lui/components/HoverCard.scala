package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class HoverCard private[components] (
    val root: HtmlElement,
    private[components] val triggerSlot: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val placementVar: Var[Popover.Placement] = Var(Popover.Placement.Bottom)
}

/** Like `Tooltip` but with rich content. Opens on hover, closes on leave. */
object HoverCard extends ComponentFactory[HoverCard] {

  val placement = Prop.in[Popover.Placement, HoverCard](_.placementVar)

  def trigger(content: Modifier[HtmlElement]*): Mod[HoverCard] = el =>
    el.triggerSlot.amend(content*)

  def body(content: Modifier[HtmlElement]*): Mod[HoverCard] = el =>
    el.bodySlot.amend(content*)

  override protected def build: HoverCard = {
    val triggerSlot = span()
    val bodySlot = div()
    val root = span(triggerSlot, bodySlot)
    val el = new HoverCard(root, triggerSlot, bodySlot)

    val hovered = Var(false)

    root.amend(
      themed(_ =>
        css.position("relative") ++
          css.display(Display.InlineFlex) ++
          css.alignItems("center")
      ),
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer
    )

    triggerSlot.amend(
      themed(_ => css.display(Display.InlineFlex) ++ css.alignItems("center"))
    )

    bodySlot.amend(
      Signal.combine(hovered.signal, el.placementVar.signal).styled {
        case (t, (visible, pl)) =>
          val (top, bottom, left, right, transform) = pl match {
            case Popover.Placement.Bottom => ("100%", "auto", "0", "auto", "translate(0, 6px)")
            case Popover.Placement.Top    => ("auto", "100%", "0", "auto", "translate(0, -6px)")
            case Popover.Placement.Right  => ("0", "auto", "100%", "auto", "translate(6px, 0)")
            case Popover.Placement.Left   => ("0", "auto", "auto", "100%", "translate(-6px, 0)")
          }
          css.position("absolute") ++
            css.raw("top", top) ++ css.raw("bottom", bottom) ++
            css.raw("left", left) ++ css.raw("right", right) ++
            css.raw("transform", transform) ++
            css.background(t.surface) ++
            css.color(t.text) ++
            css.border(Length.px(1), BorderStyle.Solid, t.border) ++
            css.borderRadius(radius.md) ++
            css.padding(spacing.lg) ++
            css.raw("box-shadow", "0 12px 28px rgba(0,0,0,0.16)") ++
            css.zIndex(30) ++
            css.opacity(if (visible) 1.0 else 0.0) ++
            css.raw("pointer-events", if (visible) "auto" else "none") ++
            css.transition("opacity", 120) ++
            css.raw("min-width", "220px")
      }
    )
    el
  }
}
