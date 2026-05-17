package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Checkbox private[components] (val root: HtmlElement) extends Component {
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val labelVar: Var[String] = Var("")
}

/** Two-state checkbox. Renders as a `button[role="checkbox"]` so it's
  * keyboard-focusable and Space/Enter toggle, with `aria-checked` reflecting
  * the state. The visual box and label sit inside the button. */
object Checkbox extends ComponentFactory[Checkbox] {

  val checked = Prop.inOut[Boolean, Checkbox](_.checkedVar)
  val disabled = Prop.in[Boolean, Checkbox](_.disabledVar)
  val label = Prop.in[String, Checkbox](_.labelVar)

  private val boxSize: Length = Length.px(16)

  override protected def build: Checkbox = {
    val box = span()
    val text = span()
    val root = button(typ := "button", box, text)
    val el = new Checkbox(root)
    val interact = Interactive.on(root)

    root.amend(
      role := "checkbox",
      aria.checked <-- el.checkedVar.signal.map(_.toString),
      aria.disabled <-- el.disabledVar.signal,
      Signal
        .combine(el.disabledVar.signal, interact.state)
        .styled { case (t, (d, i)) =>
          val ring =
            if (i.focused && !i.pressed && !d)
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
        if (!el.disabledVar.now()) el.checkedVar.update(c => !c)
      },
      // role=checkbox strips the default Space activation in some screen
      // reader setups; bind both keys explicitly to be safe.
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        if (!el.disabledVar.now() && (ev.key == " " || ev.key == "Enter")) {
          ev.preventDefault()
          el.checkedVar.update(c => !c)
        }
      }
    )

    box.amend(
      el.checkedVar.signal.styled { (t, on) =>
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
      child.maybe <-- el.checkedVar.signal.map { on =>
        if (on) Some(span(
          themed(_ => css.fontSize(Length.px(11)) ++ css.fontWeight(FontWeight.Bold) ++ css.raw("line-height", "1")),
          "✓"
        )) else None
      }
    )

    text.amend(
      typo.body,
      child.text <-- el.labelVar.signal
    )

    el
  }
}
