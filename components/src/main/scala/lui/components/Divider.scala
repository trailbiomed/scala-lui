package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Divider private[components] (val root: HtmlElement) extends Component {
  private[components] val orientationVar: Var[Divider.Orientation] = Var(Divider.Orientation.Horizontal)
  private[components] val labelVar: Var[String] = Var("")
}

object Divider extends ComponentFactory[Divider] {

  enum Orientation { case Horizontal, Vertical }

  val orientation = Prop.in[Orientation, Divider](_.orientationVar)
  val label = Prop.in[String, Divider](_.labelVar)

  override protected def build: Divider = {
    val root = div()
    val el = new Divider(root)

    val lineLeft = span()
    val lineRight = span()
    val labelEl = span()

    root.amend(
      Signal.combine(el.orientationVar.signal, el.labelVar.signal).styled {
        case (t, (o, _)) =>
          o match {
            case Orientation.Horizontal =>
              css.width(Length.pct(100)) ++ stack.row(spacing.md) ++ css.color(t.textSubtle)
            case Orientation.Vertical =>
              css.display(Display.InlineFlex) ++
                css.width(Length.px(1)) ++
                css.height(Length.auto) ++
                css.raw("align-self", "stretch") ++
                css.background(t.border)
          }
      },
      child.maybe <-- Signal.combine(el.orientationVar.signal, el.labelVar.signal).map {
        case (Orientation.Horizontal, _) => Some(lineLeft)
        case _                           => None
      },
      child.maybe <-- Signal.combine(el.orientationVar.signal, el.labelVar.signal).map {
        case (Orientation.Horizontal, lbl) if lbl.nonEmpty => Some(labelEl)
        case _                                             => None
      },
      child.maybe <-- Signal.combine(el.orientationVar.signal, el.labelVar.signal).map {
        case (Orientation.Horizontal, _) => Some(lineRight)
        case _                           => None
      }
    )

    val lineStyle = themed(t =>
      css.raw("flex", "1 1 auto") ++
        css.height(Length.px(1)) ++
        css.background(t.border)
    )
    lineLeft.amend(lineStyle)
    lineRight.amend(lineStyle)

    labelEl.amend(
      typo.hint ++ css.textTransform("uppercase") ++ css.letterSpacing(Length.em(0.05)),
      child.text <-- el.labelVar.signal
    )

    el
  }
}
