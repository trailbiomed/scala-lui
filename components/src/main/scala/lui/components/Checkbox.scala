package lui.components

import com.raquo.laminar.api.L.{Mod as _, label as labelTag, *}
import lui.*
import lui.style.*

final class Checkbox private[components] (val root: HtmlElement) extends Component {
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val labelVar: Var[String] = Var("")
}

object Checkbox extends ComponentFactory[Checkbox] {

  val checked = Prop.inOut[Boolean, Checkbox](_.checkedVar)
  val disabled = Prop.in[Boolean, Checkbox](_.disabledVar)
  val label = Prop.in[String, Checkbox](_.labelVar)

  private val boxSize: Length = Length.px(16)

  override protected def build: Checkbox = {
    val box = span()
    val text = span()
    val root = labelTag(box, text)
    val el = new Checkbox(root)

    root.amend(
      el.disabledVar.signal.styled { (_, d) =>
        stack.row(spacing.md) ++
          css.cursor(if (d) "not-allowed" else "pointer") ++
          css.opacity(if (d) 0.55 else 1.0) ++
          css.raw("user-select", "none")
      },
      onClick.mapToUnit.filter(_ => !el.disabledVar.now()) -->
        Observer[Unit](_ => el.checkedVar.update(c => !c))
    )

    box.amend(
      el.checkedVar.signal.styled { (t, on) =>
        val (bg, bd) = if (on) (t.brand, t.brand) else (t.surface, t.border)
        stack.centerAll ++
          css.width(boxSize) ++ css.height(boxSize) ++
          css.borderRadius(Length.px(4)) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.background(bg) ++
          css.color(t.onBrand) ++
          css.transition("background", 120) ++
          stack.noShrink
      },
      child.maybe <-- el.checkedVar.signal.map { on =>
        if (on) Some(span(
          themed(_ => css.fontSize(Length.px(11)) ++ css.fontWeight(FontWeight.Bold) ++ css.raw("line-height", "1")),
          "✓"
        )) else None
      }
    )

    text.amend(
      typo.body,
      child.text <-- el.labelVar.signal
    )

    el
  }
}
