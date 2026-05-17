package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.keys.EventProp
import org.scalajs.dom
import lui.*
import lui.style.*

private object TooltipEvents {
  /** Bubbling focus event (unlike `focus`, which doesn't bubble). Useful when
    * we want to track focus on any descendant of a wrapper element. */
  val onFocusIn  = new EventProp[dom.FocusEvent]("focusin")
  val onFocusOut = new EventProp[dom.FocusEvent]("focusout")
}
import TooltipEvents.*

final class Tooltip private[components] (
    val root: HtmlElement,
    private[components] val triggerSlot: HtmlElement
) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val placementVar: Var[Tooltip.Placement] = Var(Tooltip.Placement.Top)
}

/** Popover for short helper text. Appears on pointer hover (mouse-only — touch
  * is a no-op since the trigger usually doubles as the activator) AND on keyboard
  * focus within the trigger, so keyboard and screen-reader users see it too. */
object Tooltip extends ComponentFactory[Tooltip] {

  enum Placement { case Top, Bottom, Left, Right }

  val label = Prop.in[String, Tooltip](_.labelVar)
  val placement = Prop.in[Placement, Tooltip](_.placementVar)

  /** Slot for the element that the tooltip describes. */
  def trigger(content: Modifier[HtmlElement]*): Mod[Tooltip] = el =>
    el.triggerSlot.amend(content*)

  private val nextId: () => String = {
    var n = 0
    () => { n += 1; s"lui-tooltip-$n" }
  }

  override protected def build: Tooltip = {
    val triggerSlot = span()
    val bubble = span()
    val root = span(triggerSlot, bubble)
    val el = new Tooltip(root, triggerSlot)
    val tipId = nextId()

    val hovered = Interactive.on(root).hovered
    val focusWithin: Var[Boolean] = Var(false)

    root.amend(
      themed(_ =>
        css.position("relative") ++
          css.display(Display.InlineFlex) ++
          css.alignItems("center")
      )
    )

    triggerSlot.amend(
      themed(_ => css.display(Display.InlineFlex) ++ css.alignItems("center")),
      // Use focusin/focusout (bubbling) so any focusable descendant of the
      // trigger (the typical case: IconButton) drives the tooltip too. Plain
      // `focus` doesn't bubble.
      onFocusIn.mapTo(true) --> focusWithin.writer,
      onFocusOut.mapTo(false) --> focusWithin.writer,
      aria.describedBy <-- Signal
        .combine(hovered.signal, focusWithin.signal, el.labelVar.signal)
        .map { case (h, f, lbl) =>
          if ((h || f) && lbl.nonEmpty) tipId else ""
        }
    )

    bubble.amend(
      idAttr := tipId,
      role := "tooltip",
      Signal
        .combine(
          hovered.signal,
          focusWithin.signal,
          el.labelVar.signal,
          el.placementVar.signal,
          Device.reducedMotion
        )
        .styled { case (t, (hov, foc, lbl, pl, reduce)) =>
          val visible = (hov || foc) && lbl.nonEmpty
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
          val trans = if (reduce) css.raw("transition", "none") else css.transition("opacity", 120)
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
            trans ++
            css.raw("box-shadow", "0 4px 12px rgba(0,0,0,0.24)")
        },
      child.text <-- el.labelVar.signal
    )

    el
  }
}
