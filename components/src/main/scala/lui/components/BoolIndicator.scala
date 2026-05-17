package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class BoolIndicator private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Boolean] = Var(false)
  private[components] val sizeVar: Var[Length] = Var(Length.px(12))
}

/** Themed ✓/✗ glyph for boolean cells (judge tables, status grids, etc.).
  * Picks the success colour for `true`, danger colour for `false` from the
  * current theme — no caller-side colour wiring needed. */
object BoolIndicator extends ComponentFactory[BoolIndicator] {

  val value = Prop.in[Boolean, BoolIndicator](_.valueVar)
  val size = Prop.in[Length, BoolIndicator](_.sizeVar)

  override protected def build: BoolIndicator = {
    val root = span()
    val el = new BoolIndicator(root)

    root.amend(
      Signal.combine(el.valueVar.signal, el.sizeVar.signal).styled { case (t, (v, sz)) =>
        css.color(if (v) t.success else t.danger) ++
          css.fontSize(sz) ++
          css.fontWeight(FontWeight.Bold) ++
          css.raw("line-height", "1") ++
          css.display(Display.InlineFlex) ++
          css.alignItems("center")
      },
      aria.label <-- el.valueVar.signal.map(if (_) "yes" else "no"),
      child.text <-- el.valueVar.signal.map(if (_) "✓" else "✗")
    )

    el
  }
}
