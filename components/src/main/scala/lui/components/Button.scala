package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Button private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val variantVar: Var[Button.Variant] = Var(Button.Variant.Primary)
  private[components] val sizeVar: Var[Button.Size] = Var(Button.Size.Medium)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val loadingVar: Var[Boolean] = Var(false)
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]

  val clicks: EventStream[Unit] = clickBus.events
}

object Button extends ComponentFactory[Button] {

  

  enum Variant { case Primary, Secondary, Ghost }
  enum Size { case Small, Medium }

  val label    = Prop.in[String, Button](_.labelVar)
  val variant  = Prop.in[Variant, Button](_.variantVar)
  val size     = Prop.in[Size, Button](_.sizeVar)
  val disabled = Prop.in[Boolean, Button](_.disabledVar)
  val loading  = Prop.in[Boolean, Button](_.loadingVar)
  val click    = Prop.out[Unit, Button](_.clickBus)

  override protected def build: Button = {
    val root = button(typ := "button")
    val el = new Button(root)

    val state = Signal.combine(
      el.variantVar.signal,
      el.sizeVar.signal,
      el.disabledVar.signal,
      el.loadingVar.signal,
      el.interact.state
    )

    root.amend(
      state.styled { (t, st) =>
        val (v, sz, d, l, i) = st
        styleFor(t, v, sz, d || l, i)
      },
      aria.disabled <-- el.disabledVar.signal,
      span(child.text <-- el.labelVar.signal),
      onClick.preventDefault.mapToUnit
        .filter(_ => !el.disabledVar.now() && !el.loadingVar.now())
        --> el.clickBus.writer
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
    val base =
      stack.row(spacing.sm) ++ css.justifyContent("center") ++
        css.borderRadius(radius.md) ++
        css.fontWeight(FontWeight.SemiBold) ++
        css.transition("background", 150) ++
        css.cursor(if (disabled) "not-allowed" else "pointer") ++
        css.raw("user-select", "none") ++
        css.raw("font-family", "inherit")

    val sizing = s match {
      case Size.Small  => css.padding(spacing.sm, spacing.lg) ++ css.fontSize(fontSizes.md)
      case Size.Medium => css.padding(spacing.md, spacing.xl) ++ css.fontSize(fontSizes.xl)
    }

    val variantStyle = (v, disabled, i.hovered) match {
      case (Variant.Primary, true, _) =>
        css.background(t.surfaceDim) ++ css.color(t.textSubtle) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent)
      case (Variant.Primary, false, true) =>
        css.background(t.brandHover) ++ css.color(t.onBrand) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent)
      case (Variant.Primary, false, false) =>
        css.background(t.brand) ++ css.color(t.onBrand) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent)
      case (Variant.Secondary, _, true) =>
        css.background(t.brandSoft) ++ css.color(t.brand) ++
          css.border(Length.px(1), BorderStyle.Solid, t.brand)
      case (Variant.Secondary, _, false) =>
        css.background(t.surface) ++ css.color(t.text) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border)
      case (Variant.Ghost, _, true) =>
        css.background(t.brandSoft) ++ css.color(t.brand) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent)
      case (Variant.Ghost, _, false) =>
        css.background(Color.transparent) ++ css.color(t.textMuted) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent)
    }

    base ++ sizing ++ variantStyle
  }
}
