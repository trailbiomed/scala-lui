package lui.components

import com.raquo.laminar.api.L.{
  Mod as _,
  value as htmlValue,
  disabled as htmlDisabled,
  placeholder as htmlPlaceholder,
  *
}
import lui.*
import lui.style.*

final class TextInput private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val placeholderVar: Var[String] = Var("")
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val invalidVar: Var[Boolean] = Var(false)
  private[components] val variantVar: Var[TextInput.Variant] = Var(TextInput.Variant.Text)
  private[components] val alignVar: Var[TextAlign] = Var(TextAlign.Left)
  private[components] val widthVar: Var[Length] = Var(Length.auto)
  private[components] val fontSizeVar: Var[Length] = Var(fontSizes.xl)
  private[components] val focused: Var[Boolean] = Var(false)
}

object TextInput extends ComponentFactory[TextInput] {

  enum Variant { case Text, Number }

  val value = Prop.inOut[String, TextInput](_.valueVar)

  val placeholder = Prop.in[String, TextInput](_.placeholderVar)
  val disabled = Prop.in[Boolean, TextInput](_.disabledVar)
  val invalid = Prop.in[Boolean, TextInput](_.invalidVar)
  val variant = Prop.in[Variant, TextInput](_.variantVar)
  val align = Prop.in[TextAlign, TextInput](_.alignVar)
  val width = Prop.in[Length, TextInput](_.widthVar)
  val fontSize = Prop.in[Length, TextInput](_.fontSizeVar)

  override protected def build: TextInput = {
    val root = input()
    val el = new TextInput(root)

    val state = Signal.combine(
      el.focused.signal,
      el.invalidVar.signal,
      el.alignVar.signal,
      el.widthVar.signal,
      el.disabledVar.signal,
      el.fontSizeVar.signal,
    )

    root.amend(
      state.styled { case (t, (focusedOn, invalidOn, alignVal, w, d, fs)) =>
        val (bd, shadow) =
          if (invalidOn)
            (t.danger, s"0 0 0 3px ${t.danger.alpha(0.18).toCss}")
          else if (focusedOn)
            (t.borderActive, s"0 0 0 3px ${t.brand.alpha(0.18).toCss}")
          else (t.border, "none")
        css.width(w) ++
          css.padding(Length.px(7), Length.px(10)) ++
          css.border(Length.px(1), BorderStyle.Solid, bd) ++
          css.borderRadius(radius.md) ++
          css.fontSize(fs) ++
          css.fontWeight(FontWeight.Regular) ++
          css.color(t.text) ++
          css.background(if (d) t.surfaceDim else t.surface) ++
          css.textAlign(alignVal) ++
          css.raw("font-family", "inherit") ++
          // Native <input> elements get their own UA-default font
          // rendering that ignores the body's font-smoothing
          // inheritance on macOS Retina — explicitly apply
          // antialiased smoothing here so the text isn't visually
          // lighter than the surrounding chrome.
          css.raw("-webkit-font-smoothing", "antialiased") ++
          css.raw("-moz-osx-font-smoothing", "grayscale") ++
          css.raw("outline", "none") ++
          css.raw("box-shadow", shadow) ++
          css.transition("border-color", 150) ++
          css.raw("box-sizing", "border-box")
      },
      typ <-- el.variantVar.signal.map {
        case Variant.Text   => "text"
        case Variant.Number => "number"
      },
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
