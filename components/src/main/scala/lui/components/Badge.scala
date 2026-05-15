package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Badge private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val variantVar: Var[Badge.Variant] = Var(Badge.Variant.Neutral)
  private[components] val dotVar: Var[Boolean] = Var(false)
}

/** A compact count or status indicator. Different from `Tag`: `Badge` is smaller, has no
  * dismiss affordance, and supports a `dot := true` mode that renders a tiny colored circle
  * (no label content). Use for notification counts, status pips, etc. */
object Badge extends ComponentFactory[Badge] {

  enum Variant { case Brand, Success, Warning, Danger, Info, Neutral }

  val label = Prop.in[String, Badge](_.labelVar)
  val variant = Prop.in[Variant, Badge](_.variantVar)
  val dot = Prop.in[Boolean, Badge](_.dotVar)

  override protected def build: Badge = {
    val root = span()
    val el = new Badge(root)

    root.amend(
      Signal.combine(el.variantVar.signal, el.dotVar.signal).styled { case (t, (v, isDot)) =>
        val (bg, fg, bd) = colorsFor(t, v)
        if (isDot)
          css.display(Display.InlineFlex) ++
            css.width(Length.px(8)) ++
            css.height(Length.px(8)) ++
            css.borderRadius(radius.pill) ++
            css.background(fg) ++
            css.border(Length.px(1), BorderStyle.Solid, bd) ++
            stack.noShrink
        else
          css.display(Display.InlineFlex) ++
            css.alignItems("center") ++ css.justifyContent("center") ++
            css.minWidth(Length.px(18)) ++
            css.height(Length.px(18)) ++
            css.padding(Length.px(0), Length.px(6)) ++
            css.borderRadius(radius.pill) ++
            css.fontSize(fontSizes.xs) ++
            css.fontWeight(FontWeight.Bold) ++
            css.background(bg) ++ css.color(fg) ++
            css.border(Length.px(1), BorderStyle.Solid, bd) ++
            css.raw("line-height", "1") ++
            stack.noShrink
      },
      child.text <-- Signal.combine(el.dotVar.signal, el.labelVar.signal).map {
        case (true, _)  => ""
        case (_, lbl)   => lbl
      }
    )
    el
  }

  private def colorsFor(t: Theme, v: Variant): (Color, Color, Color) = v match {
    case Variant.Brand   => (t.brandSoft, t.brand, t.brand)
    case Variant.Success => (t.successSoft, t.success, t.successBorder)
    case Variant.Warning => (t.warningSoft, t.warning, t.warningBorder)
    case Variant.Danger  => (t.dangerSoft, t.danger, t.dangerBorder)
    case Variant.Info    => (t.infoSoft, t.info, t.infoBorder)
    case Variant.Neutral => (t.surfaceDim, t.textMuted, t.border)
  }
}
