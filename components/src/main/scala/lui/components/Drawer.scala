package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Drawer private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(360))
  private[components] val titleVar: Var[String] = Var("")
  private[components] val sideVar: Var[Drawer.Side] = Var(Drawer.Side.Right)
}

/** Side panel overlay. Backdrop click closes; slides from left or right. */
object Drawer extends ComponentFactory[Drawer] {

  enum Side { case Left, Right }

  val open = Prop.inOut[Boolean, Drawer](_.openVar)
  val width = Prop.in[Length, Drawer](_.widthVar)
  val title = Prop.in[String, Drawer](_.titleVar)
  val side = Prop.in[Side, Drawer](_.sideVar)

  def body(content: Modifier[HtmlElement]*): Mod[Drawer] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Drawer = {
    val bodySlot = div()
    val panel = div()
    val root = div(panel)
    val el = new Drawer(root, bodySlot)

    root.amend(
      el.openVar.signal.styled { (t, isOpen) =>
        css.position("fixed") ++
          css.top(Length.px(0)) ++
          css.right(Length.px(0)) ++
          css.bottom(Length.px(0)) ++
          css.left(Length.px(0)) ++
          css.background(t.backdrop) ++
          css.zIndex(40) ++
          css.display(if (isOpen) Display.Block else Display.None)
      },
      onClick.mapToUnit.filter(_ => el.openVar.now()) -->
        Observer[Unit](_ => el.openVar.set(false))
    )

    panel.amend(
      Signal.combine(el.widthVar.signal, el.sideVar.signal, el.openVar.signal).styled {
        case (t, (w, s, isOpen)) =>
          val (anchorLeft, anchorRight, slideFrom) = s match {
            case Side.Right => ("auto", "0", if (isOpen) "translateX(0)" else "translateX(100%)")
            case Side.Left  => ("0", "auto", if (isOpen) "translateX(0)" else "translateX(-100%)")
          }
          css.position("fixed") ++
            css.top(Length.px(0)) ++
            css.bottom(Length.px(0)) ++
            css.raw("left", anchorLeft) ++
            css.raw("right", anchorRight) ++
            css.width(w) ++
            css.maxWidth(Length.pct(100)) ++
            css.background(t.surface) ++
            css.raw("box-shadow", "0 0 32px rgba(0,0,0,0.2)") ++
            css.raw("transform", slideFrom) ++
            css.transition("transform", 200) ++
            css.padding(spacing.xl) ++
            stack.col(spacing.lg) ++
            css.raw("overflow-y", "auto") ++
            css.raw("box-sizing", "border-box")
      },
      onClick.stopPropagation.mapToUnit --> Observer[Unit](_ => ()),
      div(
        stack.between(),
        span(typo.h2 ++ css.margin(Length.px(0)), child.text <-- el.titleVar.signal),
        CloseButton(CloseButton.click.foreach(_ => el.openVar.set(false)))
      ),
      bodySlot
    )

    el
  }
}
