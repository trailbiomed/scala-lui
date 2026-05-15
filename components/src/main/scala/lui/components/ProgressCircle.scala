package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ProgressCircle private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Double] = Var(0.0)
  private[components] val variantVar: Var[ProgressCircle.Variant] = Var(ProgressCircle.Variant.Brand)
  private[components] val sizeVar: Var[Length] = Var(Length.px(40))
  private[components] val thicknessVar: Var[Length] = Var(Length.px(5))
  private[components] val showLabelVar: Var[Boolean] = Var(false)
}

/** Circular progress indicator built with `conic-gradient` for the filled arc and a centered
  * inner disc to make a donut. Set `showLabel := true` to render the percentage in the
  * center. `value` is in `[0.0, 1.0]`; out-of-range values clamp. */
object ProgressCircle extends ComponentFactory[ProgressCircle] {

  enum Variant { case Brand, Success, Warning, Danger, Info }

  val value = Prop.in[Double, ProgressCircle](_.valueVar)
  val variant = Prop.in[Variant, ProgressCircle](_.variantVar)
  val size = Prop.in[Length, ProgressCircle](_.sizeVar)
  val thickness = Prop.in[Length, ProgressCircle](_.thicknessVar)
  val showLabel = Prop.in[Boolean, ProgressCircle](_.showLabelVar)

  override protected def build: ProgressCircle = {
    val inner = div()
    val labelEl = span()
    val root = div(inner, labelEl)
    val el = new ProgressCircle(root)

    val state = Signal.combine(
      el.valueVar.signal,
      el.variantVar.signal,
      el.sizeVar.signal,
      el.thicknessVar.signal
    )

    root.amend(
      state.styled { case (t, (v, variant, sz, _)) =>
        val pct = math.max(0.0, math.min(1.0, v)) * 100.0
        val barColor = colorFor(t, variant).toCss
        val trackColor = t.surfaceDim.toCss
        css.display(Display.InlineFlex) ++
          css.alignItems("center") ++ css.justifyContent("center") ++
          css.position("relative") ++
          css.width(sz) ++ css.height(sz) ++
          css.borderRadius(radius.pill) ++
          css.raw(
            "background",
            s"conic-gradient($barColor 0 $pct%, $trackColor $pct% 100%)"
          ) ++
          css.transition("background", 220)
      }
    )

    inner.amend(
      Signal.combine(el.sizeVar.signal, el.thicknessVar.signal).styled {
        case (t, (sz, th)) =>
          css.position("absolute") ++
            css.background(t.surface) ++
            css.borderRadius(radius.pill) ++
            css.raw("width", s"calc(${sz.toCss} - 2 * ${th.toCss})") ++
            css.raw("height", s"calc(${sz.toCss} - 2 * ${th.toCss})")
      }
    )

    labelEl.amend(
      Signal.combine(el.showLabelVar.signal, el.valueVar.signal, el.sizeVar.signal).styled {
        case (t, (showLbl, v, _)) =>
          css.position("relative") ++
            css.fontSize(fontSizes.sm) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.color(t.text) ++
            css.display(if (showLbl) Display.InlineFlex else Display.None) ++
            css.raw("line-height", "1") ++
            (Style.empty :+ Decl("opacity", if (v >= 0) "1" else "1"))
      },
      child.text <-- el.valueVar.signal.map { v =>
        val pct = math.max(0.0, math.min(1.0, v)) * 100.0
        s"${pct.round.toInt}%"
      }
    )

    el
  }

  private def colorFor(t: Theme, v: Variant): Color = v match {
    case Variant.Brand   => t.brand
    case Variant.Success => t.success
    case Variant.Warning => t.warning
    case Variant.Danger  => t.danger
    case Variant.Info    => t.info
  }
}
