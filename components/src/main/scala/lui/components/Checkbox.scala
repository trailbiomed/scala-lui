package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Checkbox private[components] (val root: HtmlElement) extends Component {
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val labelVar: Var[String] = Var("")
  private[components] val indeterminateVar: Var[Boolean] = Var(false)
}

/** Checkbox. Renders as a `button[role="checkbox"]` so it's keyboard-focusable and
  * Space/Enter toggle, with `aria-checked` reflecting the state. The visual box and label
  * sit inside the button.
  *
  * `indeterminate := true` is the third, mixed state a "select all" box needs when only
  * some of its children are checked: the box shows a dash, `aria-checked` reports
  * `"mixed"`, and activating it resolves to checked, as a native mixed checkbox does. */
object Checkbox extends ComponentFactory[Checkbox] {

  val checked = Prop.inOut[Boolean, Checkbox](_.checkedVar)
  val disabled = Prop.in[Boolean, Checkbox](_.disabledVar)
  val label = Prop.in[String, Checkbox](_.labelVar)

  /** Mixed state — neither checked nor unchecked. Takes precedence over `checked` in what
    * the box draws and reports; activating the checkbox clears it and checks the box. */
  val indeterminate = Prop.in[Boolean, Checkbox](_.indeterminateVar)

  private val boxSize: Length = Length.px(16)

  private def dash: HtmlElement =
    span(
      css.width(Length.px(9)) ++
        css.height(Length.px(2)) ++
        css.borderRadius(Length.px(1)) ++
        css.raw("background", "currentColor")
    )

  /** A mixed checkbox resolves to checked, matching a native `<input>` whose
    * `indeterminate` flag is set. */
  private def toggle(el: Checkbox): Unit =
    if (el.indeterminateVar.now()) el.checkedVar.set(true)
    else el.checkedVar.update(c => !c)

  override protected def build: Checkbox = {
    val box = span()
    val text = span()
    val root = button(typ := "button", box, text)
    val el = new Checkbox(root)
    val interact = Interactive.on(root)

    root.amend(
      role := "checkbox",
      aria.checked <-- Signal
        .combine(el.checkedVar.signal, el.indeterminateVar.signal)
        .map { case (on, mixed) => if (mixed) "mixed" else on.toString },
      aria.disabled <-- el.disabledVar.signal,
      Signal
        .combine(el.disabledVar.signal, interact.state)
        .styled { case (t, (d, i)) =>
          val ring =
            if (i.focusVisible && !i.pressed && !d)
              css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.3).toCss}")
            else css.raw("box-shadow", "none")
          stack.row(spacing.md) ++
            css.alignItems("center") ++
            css.background(Color.transparent) ++
            css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
            css.borderRadius(radius.sm) ++
            css.padding(Length.px(2)) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.opacity(if (d) 0.55 else 1.0) ++
            css.raw("font-family", "inherit") ++
            css.raw("text-align", "left") ++
            css.raw("user-select", "none") ++
            css.raw("outline", "none") ++
            ring
        },
      onClick.preventDefault.mapToUnit --> Observer[Unit] { _ =>
        if (!el.disabledVar.now()) toggle(el)
      },
      // role=checkbox strips the default Space activation in some screen
      // reader setups; bind both keys explicitly to be safe.
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        if (!el.disabledVar.now() && (ev.key == " " || ev.key == "Enter")) {
          ev.preventDefault()
          toggle(el)
        }
      }
    )

    val filled = Signal
      .combine(el.checkedVar.signal, el.indeterminateVar.signal)
      .map { case (on, mixed) => mixed || on }

    box.amend(
      filled.styled { (t, on) =>
        val (bg, bd) = if (on) (t.brand, t.brand) else (t.surface, t.border)
        stack.centerAll ++
          css.width(boxSize) ++ css.height(boxSize) ++
          css.borderRadius(Length.px(4)) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.background(bg) ++
          css.color(t.onBrand) ++
          css.transition("background", 120) ++
          stack.noShrink
      },
      child.maybe <-- Signal
        .combine(el.checkedVar.signal, el.indeterminateVar.signal)
        .map { case (on, mixed) =>
          if (mixed) Some(dash) else if (on) Some(Checkmark(Length.px(11))) else None
        }
    )

    text.amend(
      typo.body,
      child.text <-- el.labelVar.signal
    )

    el
  }
}
