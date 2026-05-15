package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class RadioCard private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val optionsVar: Var[Seq[RadioCard.Option]] = Var(Seq.empty)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val orientationVar: Var[RadioCard.Orientation] =
    Var(RadioCard.Orientation.Vertical)
}

/** Card-style radio group. Each option is a clickable card with title + description. */
object RadioCard extends ComponentFactory[RadioCard] {

  enum Orientation { case Horizontal, Vertical }

  final case class Option(key: String, title: String, description: String = "")

  val value = Prop.inOut[String, RadioCard](_.valueVar)
  val options = Prop.in[Seq[Option], RadioCard](_.optionsVar)
  val disabled = Prop.in[Boolean, RadioCard](_.disabledVar)
  val orientation = Prop.in[Orientation, RadioCard](_.orientationVar)

  override protected def build: RadioCard = {
    val root = div()
    val el = new RadioCard(root)

    root.amend(
      el.orientationVar.signal.styled { (_, o) =>
        o match {
          case Orientation.Vertical   => stack.col(spacing.md)
          case Orientation.Horizontal => stack.row(spacing.md) ++ stack.wrap
        }
      },
      children <-- Signal
        .combine(el.optionsVar.signal, el.valueVar.signal, el.disabledVar.signal)
        .map { case (opts, current, d) =>
          opts.map(o => card(o, current == o.key, d, el.valueVar)).toList
        }
    )
    el
  }

  private def card(o: Option, selected: Boolean, disabled: Boolean, v: Var[String]): HtmlElement = {
    val dot = span()
    val root = div()
    val interact = Interactive.on(root)

    root.amend(
      interact.state.styled { (t, i) =>
        val bd =
          if (disabled) t.border
          else if (selected) t.brand
          else if (i.hovered) t.borderActive
          else t.border
        stack.row(spacing.md) ++
          css.alignItems("flex-start") ++
          css.padding(spacing.lg) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.borderRadius(radius.md) ++
          css.background(if (selected) t.brandSoft else t.surface) ++
          css.cursor(if (disabled) "not-allowed" else "pointer") ++
          css.opacity(if (disabled) 0.55 else 1.0) ++
          css.raw("flex", "1 1 0") ++
          css.transition("border-color", 150)
      },
      onClick.mapToUnit.filter(_ => !disabled) -->
        Observer[Unit](_ => v.set(o.key)),
      dot,
      div(
        stack.col(spacing.xs),
        span(typo.label, o.title),
        if (o.description.nonEmpty) span(typo.muted, o.description) else emptyNode
      )
    )

    dot.amend(
      themed(t =>
        stack.centerAll ++
          css.width(Length.px(16)) ++ css.height(Length.px(16)) ++
          css.borderRadius(radius.pill) ++
          css.border(Length.px(1.5), BorderStyle.Solid, if (selected) t.brand else t.border) ++
          css.background(t.surface) ++
          stack.noShrink ++
          css.raw("margin-top", "2px")
      ),
      if (selected) Radiomark() else emptyNode
    )

    root
  }
}
