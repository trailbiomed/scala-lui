package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ProgressBar private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Double] = Var(0.0)
  private[components] val variantVar: Var[ProgressBar.Variant] = Var(ProgressBar.Variant.Brand)
  private[components] val heightVar: Var[Length] = Var(Length.px(6))
  private[components] val indeterminateVar: Var[Boolean] = Var(false)
}

/** Linear progress indicator. `value` is in `[0.0, 1.0]`; out-of-range values clamp. Use
  * `indeterminate := true` for activity without a known fraction. */
object ProgressBar extends ComponentFactory[ProgressBar] {

  enum Variant { case Brand, Success, Warning, Danger, Info }

  val value = Prop.in[Double, ProgressBar](_.valueVar)
  val variant = Prop.in[Variant, ProgressBar](_.variantVar)
  val height = Prop.in[Length, ProgressBar](_.heightVar)
  val indeterminate = Prop.in[Boolean, ProgressBar](_.indeterminateVar)

  override protected def build: ProgressBar = {
    val fill = span()
    val root = div(fill)
    val el = new ProgressBar(root)

    root.amend(
      el.heightVar.signal.styled { (t, h) =>
        css.width(Length.pct(100)) ++
          css.height(h) ++
          css.borderRadius(radius.pill) ++
          css.background(t.surfaceDim) ++
          css.overflow("hidden") ++
          css.position("relative")
      }
    )

    fill.amend(
      Signal
        .combine(el.valueVar.signal, el.variantVar.signal, el.indeterminateVar.signal)
        .styled { case (t, (v, variant, indet)) =>
          val pct = math.max(0.0, math.min(1.0, v)) * 100.0
          val barColor = colorFor(t, variant)
          val widthCss = if (indet) "40%" else s"$pct%"
          val leftCss = if (indet) "30%" else "0"
          css.display(Display.Block) ++
            css.height(Length.pct(100)) ++
            css.raw("width", widthCss) ++
            css.raw("margin-left", leftCss) ++
            css.background(barColor) ++
            css.borderRadius(radius.pill) ++
            css.transition("width", 220)
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
