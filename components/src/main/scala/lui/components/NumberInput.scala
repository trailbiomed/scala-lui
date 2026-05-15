package lui.components

import com.raquo.laminar.api.L.{
  Mod as _,
  value as htmlValue,
  disabled as htmlDisabled,
  *
}
import lui.*
import lui.style.*

final class NumberInput private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Double] = Var(0.0)
  private[components] val minVar: Var[Double] = Var(Double.NegativeInfinity)
  private[components] val maxVar: Var[Double] = Var(Double.PositiveInfinity)
  private[components] val stepVar: Var[Double] = Var(1.0)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(120))
  private[components] val focused: Var[Boolean] = Var(false)
}

/** A numeric input with two stepper buttons on the right. `value` is a `Double`. `min`,
  * `max` clamp on commit; `step` is the increment used by the buttons (the user can still
  * type any value between commits). Default `step` is `1.0`. */
object NumberInput extends ComponentFactory[NumberInput] {

  val value = Prop.inOut[Double, NumberInput](_.valueVar)
  val min = Prop.in[Double, NumberInput](_.minVar)
  val max = Prop.in[Double, NumberInput](_.maxVar)
  val step = Prop.in[Double, NumberInput](_.stepVar)
  val disabled = Prop.in[Boolean, NumberInput](_.disabledVar)
  val width = Prop.in[Length, NumberInput](_.widthVar)

  override protected def build: NumberInput = {
    // Use type="text" instead of type="number" to suppress WebKit's native spinner
    // buttons (which can't be hidden with inline styles — they need ::-webkit-*-spin-button
    // pseudo-element rules in a stylesheet, and lui has none). `inputmode` gives mobile
    // browsers a numeric keypad; we parse the value ourselves anyway.
    val inputEl = input(typ := "text", inputMode := "decimal")
    val upBtn = button(typ := "button")
    val downBtn = button(typ := "button")
    val buttons = div(upBtn, downBtn)
    val root = div(inputEl, buttons)
    val el = new NumberInput(root)

    def clamp(v: Double): Double = {
      val lo = el.minVar.now()
      val hi = el.maxVar.now()
      math.max(lo, math.min(hi, v))
    }

    def bump(direction: Int): Unit = {
      val current = el.valueVar.now()
      el.valueVar.set(clamp(current + direction * el.stepVar.now()))
    }

    root.amend(
      Signal.combine(el.focused.signal, el.disabledVar.signal, el.widthVar.signal).styled {
        case (t, (focusedOn, d, w)) =>
          val bd = if (focusedOn) t.borderActive else t.border
          val shadow =
            if (focusedOn) s"0 0 0 3px ${t.brand.alpha(0.18).toCss}" else "none"
          stack.row() ++
            css.width(w) ++
            css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
            css.borderRadius(radius.md) ++
            css.background(if (d) t.surfaceDim else t.surface) ++
            css.raw("box-shadow", shadow) ++
            css.transition("border-color", 150) ++
            css.overflow("hidden")
      }
    )

    inputEl.amend(
      themed(t =>
        stack.grow ++
          css.padding(Length.px(7), Length.px(10)) ++
          css.fontSize(Length.px(15)) ++
          css.color(t.text) ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          css.raw("box-sizing", "border-box") ++
          css.raw("min-width", "0") ++
          css.raw("appearance", "textfield") ++
          css.raw("-moz-appearance", "textfield")
      ),
      htmlValue <-- el.valueVar.signal.map(formatValue),
      htmlDisabled <-- el.disabledVar.signal,
      onInput.mapToValue.map(parseInput) -->
        Observer[Option[Double]](_.foreach(v => el.valueVar.set(v))),
      onBlur --> Observer[org.scalajs.dom.FocusEvent] { _ =>
        el.focused.set(false)
        el.valueVar.update(clamp)
      },
      onFocus.mapTo(true) --> el.focused.writer
    )

    buttons.amend(
      themed(t =>
        stack.col() ++
          css.raw("border-left", s"1px solid ${t.border.toCss}") ++
          stack.noShrink
      )
    )

    upBtn.amend(stepBtnStyle, "▴", onClick.mapToUnit.filter(_ => !el.disabledVar.now()) -->
      Observer[Unit](_ => bump(1)))
    downBtn.amend(stepBtnStyle, "▾", onClick.mapToUnit.filter(_ => !el.disabledVar.now()) -->
      Observer[Unit](_ => bump(-1)))

    el
  }

  private val stepBtnStyle = themed(t =>
    stack.centerAll ++
      css.width(Length.px(22)) ++ css.height(Length.px(17)) ++
      css.background(Color.transparent) ++
      css.color(t.textMuted) ++
      css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
      css.fontSize(fontSizes.xs) ++
      css.cursor("pointer") ++
      css.raw("font-family", "inherit") ++
      css.raw("user-select", "none") ++
      css.raw("line-height", "1")
  )

  private def formatValue(d: Double): String = {
    if (d.isNaN) ""
    else if (d == d.toLong.toDouble) d.toLong.toString
    else d.toString
  }

  private def parseInput(s: String): Option[Double] = {
    val trimmed = s.trim
    if (trimmed.isEmpty) None
    else trimmed.toDoubleOption
  }
}
