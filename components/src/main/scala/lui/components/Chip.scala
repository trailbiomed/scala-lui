package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

/** Pill-shaped toggle chip. Two visual states (idle / active) plus hover and
  * focus affordances. Active state is owned by the caller — typically a
  * parent maintains a `Var[T]` of "current selection" and feeds each chip an
  * `active <-- currentSig.map(_ == thisValue)` binding; the chip emits clicks
  * upward, the parent decides what to write.
  *
  * Renders as a native `<button>` so it participates in the tab order and
  * Space/Enter activate it without extra handlers. */
final class Chip private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String]    = Var("")
  private[components] val activeVar: Var[Boolean]  = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val clickBus: EventBus[Unit] = new EventBus

  val clicks: EventStream[Unit] = clickBus.events
}

object Chip extends ComponentFactory[Chip] {

  val label    = Prop.in[String, Chip](_.labelVar)
  val active   = Prop.in[Boolean, Chip](_.activeVar)
  val disabled = Prop.in[Boolean, Chip](_.disabledVar)
  val click    = Prop.out[Unit, Chip](_.clickBus)

  override protected def build: Chip = {
    val root = button(typ := "button")
    val el = new Chip(root)

    val state = Signal.combine(el.activeVar.signal, el.disabledVar.signal, el.interact.state)

    root.amend(
      state.styled { (t, st) =>
        val (active, disabled, i) = st
        styleFor(t, active, disabled, i)
      },
      aria.pressed <-- el.activeVar.signal.map(_.toString),
      aria.disabled <-- el.disabledVar.signal,
      span(child.text <-- el.labelVar.signal),
      onClick.preventDefault.mapToUnit.filter(_ => !el.disabledVar.now()) --> el.clickBus.writer,
    )
    el
  }

  private def styleFor(t: Theme, active: Boolean, disabled: Boolean, i: InteractionState): Style = {
    val base =
      css.padding(Length.px(6), spacing.lg) ++
        css.fontSize(fontSizes.md) ++
        css.fontWeight(FontWeight.SemiBold) ++
        css.borderRadius(radius.pill) ++
        css.cursor(if (disabled) "not-allowed" else "pointer") ++
        css.transition("background", 150) ++
        css.selectNone ++
        css.raw("font-family", "inherit") ++
        css.raw("outline", "none")

    val colors =
      if (disabled)
        css.background(t.surfaceDim) ++
          css.color(t.textSubtle) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.opacity(0.6)
      else if (active)
        css.background(t.brand) ++
          css.color(t.onBrand) ++
          css.border(Length.px(1), BorderStyle.Solid, t.brand)
      else if (i.hovered)
        css.background(t.brandSoft) ++
          css.color(t.brand) ++
          css.border(Length.px(1), BorderStyle.Solid, t.borderActive)
      else
        css.background(t.surface) ++
          css.color(t.text) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border)

    val focusRing =
      if (i.focused && !i.pressed && !disabled)
        css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.3).toCss}")
      else css.raw("box-shadow", "none")

    base ++ colors ++ focusRing
  }
}
