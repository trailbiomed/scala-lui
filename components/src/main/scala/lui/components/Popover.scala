package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Popover private[components] (
    val root: HtmlElement,
    private[components] val triggerSlot: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val placementVar: Var[Popover.Placement] = Var(Popover.Placement.Bottom)
}

/** Anchored, click-toggled overlay panel. Building block for `Menu`, `HoverCard`,
  * `ToggleTip`. Click outside closes it. */
object Popover extends ComponentFactory[Popover] {

  enum Placement { case Top, Bottom, Left, Right }

  val open = Prop.inOut[Boolean, Popover](_.openVar)
  val placement = Prop.in[Placement, Popover](_.placementVar)

  def trigger(content: Modifier[HtmlElement]*): Mod[Popover] = el =>
    el.triggerSlot.amend(content*)

  def body(content: Modifier[HtmlElement]*): Mod[Popover] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Popover = {
    val triggerSlot = span()
    val bodySlot = div()
    val root = span(triggerSlot, bodySlot)
    val el = new Popover(root, triggerSlot, bodySlot)

    var outsideHandler: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = null

    root.amend(
      themed(_ =>
        css.position("relative") ++
          css.display(Display.InlineFlex) ++
          css.alignItems("center")
      ),
      onMountCallback { _ =>
        outsideHandler = (ev: dom.MouseEvent) => {
          if (el.openVar.now() && !root.ref.contains(ev.target.asInstanceOf[dom.Node]))
            el.openVar.set(false)
        }
        dom.document.addEventListener("mousedown", outsideHandler)
      },
      onUnmountCallback { _ =>
        if (outsideHandler != null) {
          dom.document.removeEventListener("mousedown", outsideHandler)
          outsideHandler = null
        }
      }
    )

    triggerSlot.amend(
      themed(_ => css.display(Display.InlineFlex) ++ css.alignItems("center")),
      onClick.mapToUnit --> Observer[Unit](_ => el.openVar.update(b => !b))
    )

    bodySlot.amend(
      Signal.combine(el.openVar.signal, el.placementVar.signal).styled {
        case (t, (isOpen, pl)) =>
          val (top, bottom, left, right, transform) = pl match {
            case Placement.Bottom =>
              ("100%", "auto", "0", "auto", "translate(0, 6px)")
            case Placement.Top =>
              ("auto", "100%", "0", "auto", "translate(0, -6px)")
            case Placement.Right =>
              ("0", "auto", "100%", "auto", "translate(6px, 0)")
            case Placement.Left =>
              ("0", "auto", "auto", "100%", "translate(-6px, 0)")
          }
          css.position("absolute") ++
            css.raw("top", top) ++
            css.raw("bottom", bottom) ++
            css.raw("left", left) ++
            css.raw("right", right) ++
            css.raw("transform", transform) ++
            css.background(t.surface) ++
            css.color(t.text) ++
            css.border(Length.px(1), BorderStyle.Solid, t.border) ++
            css.borderRadius(radius.md) ++
            css.padding(spacing.md) ++
            css.raw("box-shadow", "0 12px 28px rgba(0,0,0,0.16)") ++
            css.zIndex(30) ++
            css.display(if (isOpen) Display.Block else Display.None) ++
            css.raw("min-width", "180px")
      }
    )

    el
  }
}
