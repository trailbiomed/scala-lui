package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Accordion private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val titleVar: Var[String] = Var("")
  private[components] val summaryVar: Var[String] = Var("")
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val headerHover: Var[Boolean] = Var(false)
}

object Accordion extends ComponentFactory[Accordion] {

  val title = Prop.in[String, Accordion](_.titleVar)
  val summary = Prop.in[String, Accordion](_.summaryVar)
  val open = Prop.inOut[Boolean, Accordion](_.openVar)

  def body(content: Modifier[HtmlElement]*): Mod[Accordion] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Accordion = {
    val bodySlot = div()
    val bodyContainer = div(bodySlot)
    val headerBtn = button(typ := "button")
    val root = div(headerBtn, bodyContainer)

    val el = new Accordion(root, bodySlot)

    bodySlot.amend(
      themed(t =>
        stack.col(spacing.lg) ++
          css.padding(spacing.lg, Length.px(0)) ++
          css.raw("border-top", s"1px solid ${t.border.toCss}") ++
          css.raw("margin-top", spacing.lg.toCss)
      )
    )

    headerBtn.amend(
      el.headerHover.signal.styled { (t, hov) =>
        val baseColor = if (hov) t.text else t.textMuted
        css.width(Length.pct(100)) ++
          stack.between() ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.color(baseColor) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(FontWeight.Medium) ++
          css.padding(Length.px(0)) ++
          css.cursor("pointer") ++
          css.raw("font-family", "inherit") ++
          css.transition("color", 120)
      },
      onMouseEnter.mapTo(true) --> el.headerHover.writer,
      onMouseLeave.mapTo(false) --> el.headerHover.writer,
      onClick.mapToUnit --> Observer[Unit](_ => el.openVar.update(o => !o)),
      span(child.text <-- el.titleVar.signal),
      div(
        stack.row(spacing.md),
        span(typo.muted, child.text <-- el.summaryVar.signal),
        span(
          el.openVar.signal.styled { (_, isOpen) =>
            stack.row() ++ css.transition("transform", 200) ++
              css.raw("transform", if (isOpen) "rotate(180deg)" else "rotate(0deg)")
          },
          "▾"
        )
      )
    )

    bodyContainer.amend(
      el.openVar.signal.styled { (_, isOpen) =>
        css.overflow("hidden") ++
          css.transition("max-height", 250) ++
          css.raw("max-height", if (isOpen) "1000px" else "0")
      }
    )

    el
  }
}
