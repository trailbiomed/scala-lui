package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ReferenceCard private[components] (val root: HtmlElement) extends Component {
  private[components] val nameVar: Var[String] = Var("")
  private[components] val iconVar: Var[String] = Var("◉")
  private[components] val sourceLabelVar: Var[String] = Var("")
  private[components] val sampleCountVar: Var[Int] = Var(0)
  private[components] val organismVar: Var[String] = Var("")
  private[components] val descriptionVar: Var[String] = Var("")
  private[components] val lastUsedVar: Var[String] = Var("")
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]
}

object ReferenceCard extends ComponentFactory[ReferenceCard] {

  val name = Prop.in[String, ReferenceCard](_.nameVar)
  val icon = Prop.in[String, ReferenceCard](_.iconVar)
  val sourceLabel = Prop.in[String, ReferenceCard](_.sourceLabelVar)
  val sampleCount = Prop.in[Int, ReferenceCard](_.sampleCountVar)
  val organism = Prop.in[String, ReferenceCard](_.organismVar)
  val description = Prop.in[String, ReferenceCard](_.descriptionVar)
  val lastUsed = Prop.in[String, ReferenceCard](_.lastUsedVar)
  val click = Prop.out[Unit, ReferenceCard](_.clickBus)

  override protected def build: ReferenceCard = {
    val root = div()
    val el = new ReferenceCard(root)

    val nameStyle    = typo.label ++ css.fontWeight(FontWeight.SemiBold) ++ css.fontSize(fontSizes.xl)
    val lastUsedStyle = typo.hint ++ stack.noShrink
    val metaStyle    = typo.muted ++ stack.row(spacing.md)
    val descStyle    = typo.muted ++ css.lineHeight(1.45) ++ css.raw("margin-top", spacing.xs.toCss)

    root.amend(
      el.interact.state.styled { (t, i) =>
        val bd = if (i.hovered) t.borderActive else t.border
        css.background(t.surface) ++
          css.borderRadius(radius.lg) ++
          css.padding(spacing.lg, spacing.xl) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.cursor("pointer") ++
          css.transition("border-color", 150) ++
          stack.col(spacing.xs)
      },
      onClick.mapToUnit --> el.clickBus.writer,
      div(
        stack.between(spacing.md) ++ css.alignItems("flex-start"),
        div(
          stack.row(spacing.md),
          span(css.fontSize(fontSizes.xxxl), child.text <-- el.iconVar.signal),
          span(nameStyle, child.text <-- el.nameVar.signal)
        ),
        span(lastUsedStyle, child.text <-- el.lastUsedVar.signal)
      ),
      div(
        metaStyle,
        span(child.text <-- el.sourceLabelVar.signal),
        span("·"),
        span(child.text <-- el.sampleCountVar.signal.map(n => s"$n samples")),
        span("·"),
        span(child.text <-- el.organismVar.signal)
      ),
      div(
        descStyle,
        child.text <-- el.descriptionVar.signal
      )
    )

    el
  }
}
