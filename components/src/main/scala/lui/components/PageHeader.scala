package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class PageHeader private[components] (
    val root: HtmlElement,
    private[components] val rightContainer: HtmlElement
) extends Component {
  private[components] val titleVar: Var[String] = Var("")
  private[components] val backLabelVar: Var[String] = Var("") // empty == no back button
  private[components] val backBus: EventBus[Unit] = new EventBus[Unit]
  private[components] val backHover: Var[Boolean] = Var(false)
}

object PageHeader extends ComponentFactory[PageHeader] {

  val title = Prop.in[String, PageHeader](_.titleVar)

  /** Empty string hides the back affordance. */
  val back = Prop.in[String, PageHeader](_.backLabelVar)

  val onBack = Prop.out[Unit, PageHeader](_.backBus)

  /** Drop content into the right-hand side of the header. */
  def right(content: Modifier[HtmlElement]*): Mod[PageHeader] = el =>
    el.rightContainer.amend(content*)

  override protected def build: PageHeader = {
    val rightContainer = div(stack.row(spacing.lg))
    val titleEl = span()
    val backEl = button(typ := "button")

    val root = headerTag(
      div(stack.row(spacing.xl) ++ stack.grow, backEl, titleEl),
      rightContainer
    )

    val el = new PageHeader(root, rightContainer)

    root.amend(
      themed(t =>
        css.position("sticky") ++ css.top(Length.px(0)) ++ css.zIndex(30) ++
          css.background(t.surface) ++
          css.height(Length.px(56)) ++
          css.borderBottom(Length.px(1), BorderStyle.Solid, t.border) ++
          stack.row(spacing.xl) ++
          css.padding(Length.px(0), spacing.xxl)
      )
    )

    titleEl.amend(
      typo.label ++ css.fontWeight(FontWeight.SemiBold),
      child.text <-- el.titleVar.signal
    )

    backEl.amend(
      Signal.combine(el.backLabelVar.signal, el.backHover.signal).styled {
        case (t, (lbl, hov)) =>
          val baseColor = if (hov) t.text else t.textMuted
          val visible = lbl.nonEmpty
          css.background(Color.transparent) ++
            css.color(baseColor) ++
            css.fontSize(fontSizes.lg) ++
            css.fontWeight(FontWeight.Medium) ++
            css.padding(spacing.xs, Length.px(0)) ++
            css.cursor("pointer") ++
            css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
            css.raw("font-family", "inherit") ++
            css.transition("color", 120) ++
            css.raw("display", if (visible) "inline-flex" else "none")
      },
      child.text <-- el.backLabelVar.signal,
      onClick.mapToUnit --> el.backBus.writer,
      onMouseEnter.mapTo(true) --> el.backHover.writer,
      onMouseLeave.mapTo(false) --> el.backHover.writer
    )

    el
  }
}
