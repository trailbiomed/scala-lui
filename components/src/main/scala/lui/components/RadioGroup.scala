package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class RadioGroup private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val optionsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val orientationVar: Var[RadioGroup.Orientation] = Var(RadioGroup.Orientation.Vertical)
}

object RadioGroup extends ComponentFactory[RadioGroup] {

  enum Orientation { case Vertical, Horizontal }

  val value = Prop.inOut[String, RadioGroup](_.valueVar)
  val options = Prop.in[Seq[(String, String)], RadioGroup](_.optionsVar)
  val disabled = Prop.in[Boolean, RadioGroup](_.disabledVar)
  val orientation = Prop.in[Orientation, RadioGroup](_.orientationVar)

  private val dotSize: Length = Length.px(16)

  override protected def build: RadioGroup = {
    val root = div()
    val el = new RadioGroup(root)

    root.amend(
      el.orientationVar.signal.styled { (_, o) =>
        o match {
          case Orientation.Vertical   => stack.col(spacing.md)
          case Orientation.Horizontal => stack.row(spacing.xl) ++ stack.wrap
        }
      },
      children <-- Signal
        .combine(el.optionsVar.signal, el.valueVar.signal, el.disabledVar.signal)
        .map { case (opts, current, d) =>
          opts.map { case (key, lbl) => optionEl(key, lbl, current == key, d, el.valueVar) }.toList
        }
    )

    el
  }

  private def optionEl(
      key: String,
      lbl: String,
      selected: Boolean,
      disabled: Boolean,
      valueVar: Var[String]
  ): HtmlElement = {
    val dot = span()
    val text = span()
    val root = label(dot, text)

    root.amend(
      themed(_ =>
        stack.row(spacing.md) ++
          css.cursor(if (disabled) "not-allowed" else "pointer") ++
          css.opacity(if (disabled) 0.55 else 1.0) ++
          css.raw("user-select", "none")
      ),
      onClick.mapToUnit.filter(_ => !disabled) -->
        Observer[Unit](_ => valueVar.set(key))
    )

    dot.amend(
      themed(t =>
        stack.centerAll ++
          css.width(dotSize) ++ css.height(dotSize) ++
          css.borderRadius(radius.pill) ++
          css.border(Length.px(1.5), BorderStyle.Solid, if (selected) t.brand else t.border) ++
          css.background(t.surface) ++
          css.transition("border-color", 120) ++
          stack.noShrink
      ),
      child.maybe <-- Signal.fromValue(
        if (selected)
          Some(span(themed(t =>
            css.width(Length.px(8)) ++ css.height(Length.px(8)) ++
              css.borderRadius(radius.pill) ++ css.background(t.brand)
          )))
        else None
      )
    )

    text.amend(typo.body, lbl)
    root
  }
}
