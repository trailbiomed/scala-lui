package lui.components

import com.raquo.laminar.api.L.{
  Mod as _,
  value as htmlValue,
  disabled as htmlDisabled,
  *
}
import lui.*
import lui.style.*

final class Dropdown private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val optionsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.auto)
  private[components] val focused: Var[Boolean] = Var(false)
}

object Dropdown extends ComponentFactory[Dropdown] {

  val value = Prop.inOut[String, Dropdown](_.valueVar)

  val options = Prop.in[Seq[(String, String)], Dropdown](_.optionsVar)

  val disabled = Prop.in[Boolean, Dropdown](_.disabledVar)

  val width = Prop.in[Length, Dropdown](_.widthVar)

  override protected def build: Dropdown = {
    val root = select()
    val el = new Dropdown(root)

    root.amend(
      Signal.combine(el.focused.signal, el.disabledVar.signal, el.widthVar.signal).styled {
        case (t, (focusedOn, d, w)) =>
          val bd = if (focusedOn) t.borderActive else t.border
          val shadow =
            if (focusedOn) s"0 0 0 3px ${t.brand.alpha(0.18).toCss}" else "none"
          css.width(w) ++
            css.padding(Length.px(7), Length.px(28)) ++
            css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
            css.borderRadius(radius.md) ++
            css.fontSize(fontSizes.lg) ++
            css.color(t.text) ++
            css.background(t.surface) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.raw("appearance", "none") ++
            css.raw("-webkit-appearance", "none") ++
            css.raw("font-family", "inherit") ++
            css.raw("outline", "none") ++
            css.raw("box-shadow", shadow) ++
            css.transition("border-color", 150) ++
            css.raw(
              "background-image",
              "url(\"data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2394a3b8' d='M6 8.5L2 4.5h8z'/%3E%3C/svg%3E\")"
            ) ++
            css.raw("background-repeat", "no-repeat") ++
            css.raw("background-position", "right 10px center")
      },
      htmlValue <-- el.valueVar.signal,
      onChange.mapToValue --> el.valueVar.writer,
      onFocus.mapTo(true) --> el.focused.writer,
      onBlur.mapTo(false) --> el.focused.writer,
      htmlDisabled <-- el.disabledVar.signal,
      children <-- el.optionsVar.signal.map { opts =>
        opts.map { case (key, lbl) =>
          option(htmlValue := key, lbl)
        }
      }
    )

    el
  }
}
