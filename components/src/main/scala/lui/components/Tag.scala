package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Tag private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val variantVar: Var[Tag.Variant] = Var(Tag.Variant.Neutral)
  private[components] val removableVar: Var[Boolean] = Var(false)
  private[components] val removeBus: EventBus[Unit] = new EventBus[Unit]
}

object Tag extends ComponentFactory[Tag] {

  enum Variant { case Interesting, Warning, Neutral }

  val label = Prop.in[String, Tag](_.labelVar)
  val variant = Prop.in[Variant, Tag](_.variantVar)
  val removable = Prop.in[Boolean, Tag](_.removableVar)
  val remove = Prop.out[Unit, Tag](_.removeBus)

  override protected def build: Tag = {
    val root = span()
    val el = new Tag(root)

    val closeBtnStyle = themed(t =>
      css.cursor("pointer") ++ css.opacity(0.6) ++ css.fontSize(fontSizes.lg) ++
        css.padding(Length.px(0), Length.px(2)) ++
        css.color(t.text) ++
        css.raw("line-height", "1") ++
        css.raw("user-select", "none") ++
        css.raw("margin-left", spacing.xs.toCss)
    )

    root.amend(
      el.variantVar.signal.styled(styleFor),
      span(child.text <-- el.labelVar.signal),
      child.maybe <-- el.removableVar.signal.map { rem =>
        if (rem) {
          Some(
            span(
              closeBtnStyle,
              "×",
              onClick.stopPropagation.mapToUnit --> el.removeBus.writer
            )
          )
        } else None
      }
    )
    el
  }

  private def styleFor(t: Theme, v: Variant): Style = {
    val base =
      stack.row(spacing.xs) ++
        css.padding(Length.px(2), spacing.md) ++ css.borderRadius(radius.pill) ++
        css.fontSize(fontSizes.sm) ++ css.fontWeight(FontWeight.SemiBold)

    val colors = v match {
      case Variant.Interesting =>
        css.background(t.successSoft) ++ css.color(t.success) ++
          css.border(Length.px(1), BorderStyle.Solid, t.successBorder)
      case Variant.Warning =>
        css.background(t.dangerSoft) ++ css.color(t.danger) ++
          css.border(Length.px(1), BorderStyle.Solid, t.dangerBorder)
      case Variant.Neutral =>
        css.background(t.surfaceDim) ++ css.color(t.textMuted) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border)
    }

    base ++ colors
  }
}
