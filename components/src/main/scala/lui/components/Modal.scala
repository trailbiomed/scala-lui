package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Modal private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(380))
  private[components] val titleVar: Var[String] = Var("")
}

object Modal extends ComponentFactory[Modal] {

  val open = Prop.inOut[Boolean, Modal](_.openVar)

  val title = Prop.in[String, Modal](_.titleVar)

  val width = Prop.in[Length, Modal](_.widthVar)

  def body(content: Modifier[HtmlElement]*): Mod[Modal] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Modal = {
    val bodySlot = div()
    val cardEl = div()
    val root = div(cardEl)

    val el = new Modal(root, bodySlot)

    root.amend(
      el.openVar.signal.styled { (t, isOpen) =>
        css.position("fixed") ++
          css.top(Length.px(0)) ++
          css.right(Length.px(0)) ++
          css.bottom(Length.px(0)) ++
          css.left(Length.px(0)) ++
          css.background(t.backdrop) ++
          css.zIndex(40) ++
          css.display(if (isOpen) Display.Flex else Display.None) ++
          css.alignItems("center") ++
          css.justifyContent("center") ++
          css.padding(spacing.xl)
      },
      onClick.mapToUnit.filter(_ => el.openVar.now()) -->
        Observer[Unit](_ => el.openVar.set(false))
    )

    cardEl.amend(
      el.widthVar.signal.styled { (t, w) =>
        css.background(t.surface) ++
          css.borderRadius(Length.px(20)) ++
          css.padding(Length.px(28)) ++
          css.width(w) ++
          css.maxWidth(Length.pct(100)) ++
          css.raw("box-shadow", "0 24px 60px rgba(0,0,0,0.32)") ++
          stack.col(spacing.lg)
      },
      onClick.stopPropagation.mapToUnit --> Observer[Unit](_ => ()),
      div(
        themed(t => css.fontSize(fontSizes.xxl) ++ css.fontWeight(FontWeight.SemiBold) ++ css.color(t.text)),
        child.text <-- el.titleVar.signal
      ),
      bodySlot
    )

    el
  }
}
