package lui.components

import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, disabled as htmlDisabled, placeholder as htmlPlaceholder, *}
import lui.*
import lui.style.*

final class PasswordInput private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val placeholderVar: Var[String] = Var("")
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val invalidVar: Var[Boolean] = Var(false)
  private[components] val revealedVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.auto)
  private[components] val focused: Var[Boolean] = Var(false)
}

/** Password input with a reveal toggle. Visually mirrors `TextInput`. */
object PasswordInput extends ComponentFactory[PasswordInput] {

  val value = Prop.inOut[String, PasswordInput](_.valueVar)
  val placeholder = Prop.in[String, PasswordInput](_.placeholderVar)
  val disabled = Prop.in[Boolean, PasswordInput](_.disabledVar)
  val invalid = Prop.in[Boolean, PasswordInput](_.invalidVar)
  val width = Prop.in[Length, PasswordInput](_.widthVar)

  override protected def build: PasswordInput = {
    val inputEl = input()
    val toggleBtn = button(typ := "button")
    val root = div(inputEl, toggleBtn)
    val el = new PasswordInput(root)

    root.amend(
      Signal
        .combine(el.focused.signal, el.invalidVar.signal, el.widthVar.signal, el.disabledVar.signal)
        .styled { case (t, (focusedOn, invalidOn, w, d)) =>
          val (bd, shadow) =
            if (invalidOn) (t.danger, s"0 0 0 3px ${t.danger.alpha(0.18).toCss}")
            else if (focusedOn) (t.borderActive, s"0 0 0 3px ${t.brand.alpha(0.18).toCss}")
            else (t.border, "none")
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
      typ <-- el.revealedVar.signal.map(if (_) "text" else "password"),
      themed(t =>
        stack.grow ++
          css.padding(Length.px(7), Length.px(10)) ++
          css.fontSize(Length.px(16)) ++
          css.color(t.text) ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          css.raw("box-sizing", "border-box") ++
          css.raw("min-width", "0")
      ),
      htmlValue <-- el.valueVar.signal,
      htmlPlaceholder <-- el.placeholderVar.signal,
      htmlDisabled <-- el.disabledVar.signal,
      onInput.mapToValue --> el.valueVar.writer,
      onFocus.mapTo(true) --> el.focused.writer,
      onBlur.mapTo(false) --> el.focused.writer
    )

    toggleBtn.amend(
      themed(t =>
        stack.centerAll ++
          css.width(Length.px(32)) ++ css.height(Length.auto) ++
          css.color(t.textMuted) ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.cursor("pointer") ++
          css.fontSize(fontSizes.xl) ++
          css.raw("border-left", s"1px solid ${t.border.toCss}")
      ),
      child.text <-- el.revealedVar.signal.map(if (_) "🙈" else "👁"),
      onClick.mapToUnit --> Observer[Unit](_ => el.revealedVar.update(b => !b))
    )

    el
  }
}
