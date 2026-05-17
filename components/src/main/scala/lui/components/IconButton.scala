package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class IconButton private[components] (val root: HtmlElement) extends Component {
  private[components] val iconVar: Var[String] = Var("")
  private[components] val ariaLabelVar: Var[String] = Var("")
  private[components] val variantVar: Var[IconButton.Variant] = Var(IconButton.Variant.Ghost)
  private[components] val sizeVar: Var[IconButton.Size] = Var(IconButton.Size.Medium)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]
}

/** Square button rendering a single character/glyph icon. Always supply `ariaLabel` for
  * accessibility — there is no visible text label. */
object IconButton extends ComponentFactory[IconButton] {

  enum Variant { case Primary, Secondary, Ghost }
  enum Size { case Small, Medium }

  val icon = Prop.in[String, IconButton](_.iconVar)
  val ariaLabel = Prop.in[String, IconButton](_.ariaLabelVar)
  val variant = Prop.in[Variant, IconButton](_.variantVar)
  val size = Prop.in[Size, IconButton](_.sizeVar)
  val disabled = Prop.in[Boolean, IconButton](_.disabledVar)
  val click = Prop.out[Unit, IconButton](_.clickBus)

  override protected def build: IconButton = {
    val root = button(typ := "button")
    val el = new IconButton(root)

    val state = Signal.combine(
      el.variantVar.signal,
      el.sizeVar.signal,
      el.disabledVar.signal,
      el.interact.state
    )

    root.amend(
      state.styled { (t, st) =>
        val (v, sz, d, i) = st
        styleFor(t, v, sz, d, i)
      },
      aria.label <-- el.ariaLabelVar.signal,
      aria.disabled <-- el.disabledVar.signal,
      child.text <-- el.iconVar.signal,
      onClick.preventDefault.mapToUnit
        .filter(_ => !el.disabledVar.now()) --> el.clickBus.writer
    )
    el
  }

  private def styleFor(
      t: Theme,
      v: Variant,
      s: Size,
      disabled: Boolean,
      i: InteractionState
  ): Style = {
    val dim = s match {
      case Size.Small  => Length.px(26)
      case Size.Medium => Length.px(32)
    }
    val fontSz = s match {
      case Size.Small  => fontSizes.lg
      case Size.Medium => fontSizes.xl
    }
    val base =
      stack.centerAll ++
        css.width(dim) ++ css.height(dim) ++
        css.borderRadius(radius.md) ++
        css.fontSize(fontSz) ++
        css.fontWeight(FontWeight.Medium) ++
        css.transition("background", 150) ++
        css.cursor(if (disabled) "not-allowed" else "pointer") ++
        css.raw("user-select", "none") ++
        css.raw("font-family", "inherit") ++
        css.raw("line-height", "1") ++
        css.raw("outline", "none")

    val variantStyle = (v, disabled, i.hovered) match {
      case (_, true, _) =>
        css.background(t.surfaceDim) ++ css.color(t.textSubtle) ++
          css.border(Length.px(1.5), BorderStyle.Solid, Color.transparent)
      case (Variant.Primary, false, true) =>
        css.background(t.brandHover) ++ css.color(t.onBrand) ++
          css.border(Length.px(1.5), BorderStyle.Solid, Color.transparent)
      case (Variant.Primary, false, false) =>
        css.background(t.brand) ++ css.color(t.onBrand) ++
          css.border(Length.px(1.5), BorderStyle.Solid, Color.transparent)
      case (Variant.Secondary, false, true) =>
        css.background(t.brandSoft) ++ css.color(t.brand) ++
          css.border(Length.px(1.5), BorderStyle.Solid, t.brand)
      case (Variant.Secondary, false, false) =>
        css.background(t.surface) ++ css.color(t.text) ++
          css.border(Length.px(1.5), BorderStyle.Solid, t.border)
      case (Variant.Ghost, false, true) =>
        css.background(t.surfaceDim) ++ css.color(t.text) ++
          css.border(Length.px(1.5), BorderStyle.Solid, Color.transparent)
      case (Variant.Ghost, false, false) =>
        css.background(Color.transparent) ++ css.color(t.textMuted) ++
          css.border(Length.px(1.5), BorderStyle.Solid, Color.transparent)
    }
    val focusRing =
      if (i.focused && !i.pressed && !disabled)
        css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.35).toCss}")
      else css.raw("box-shadow", "none")
    base ++ variantStyle ++ focusRing
  }
}
