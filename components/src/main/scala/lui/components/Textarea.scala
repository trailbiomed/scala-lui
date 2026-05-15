package lui.components

import com.raquo.laminar.api.L.{
  Mod as _,
  value as htmlValue,
  disabled as htmlDisabled,
  placeholder as htmlPlaceholder,
  rows as htmlRows,
  *
}
import lui.*
import lui.style.*

final class Textarea private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val placeholderVar: Var[String] = Var("")
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val invalidVar: Var[Boolean] = Var(false)
  private[components] val rowsVar: Var[Int] = Var(4)
  private[components] val widthVar: Var[Length] = Var(Length.pct(100))
  private[components] val resizableVar: Var[Boolean] = Var(true)
  private[components] val focused: Var[Boolean] = Var(false)
}

object Textarea extends ComponentFactory[Textarea] {

  val value = Prop.inOut[String, Textarea](_.valueVar)
  val placeholder = Prop.in[String, Textarea](_.placeholderVar)
  val disabled = Prop.in[Boolean, Textarea](_.disabledVar)
  val invalid = Prop.in[Boolean, Textarea](_.invalidVar)
  val rows = Prop.in[Int, Textarea](_.rowsVar)
  val width = Prop.in[Length, Textarea](_.widthVar)
  val resizable = Prop.in[Boolean, Textarea](_.resizableVar)

  override protected def build: Textarea = {
    val root = textArea()
    val el = new Textarea(root)

    val state = Signal.combine(
      el.focused.signal,
      el.invalidVar.signal,
      el.widthVar.signal,
      el.disabledVar.signal,
      el.resizableVar.signal
    )

    root.amend(
      state.styled { case (t, (focusedOn, invalidOn, w, d, resz)) =>
        val (bd, shadow) =
          if (invalidOn) (t.danger, s"0 0 0 3px ${t.danger.alpha(0.18).toCss}")
          else if (focusedOn) (t.borderActive, s"0 0 0 3px ${t.brand.alpha(0.18).toCss}")
          else (t.border, "none")
        css.width(w) ++
          css.padding(Length.px(9), Length.px(11)) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.borderRadius(radius.md) ++
          css.fontSize(Length.px(15)) ++
          css.color(t.text) ++
          css.background(if (d) t.surfaceDim else t.surface) ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          css.raw("box-shadow", shadow) ++
          css.transition("border-color", 150) ++
          css.raw("box-sizing", "border-box") ++
          css.raw("resize", if (resz) "vertical" else "none") ++
          css.lineHeight(1.45)
      },
      htmlRows <-- el.rowsVar.signal,
      htmlValue <-- el.valueVar.signal,
      htmlPlaceholder <-- el.placeholderVar.signal,
      htmlDisabled <-- el.disabledVar.signal,
      onInput.mapToValue --> el.valueVar.writer,
      onFocus.mapTo(true) --> el.focused.writer,
      onBlur.mapTo(false) --> el.focused.writer
    )

    el
  }
}
