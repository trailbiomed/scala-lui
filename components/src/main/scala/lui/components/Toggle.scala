package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Toggle private[components] (val root: HtmlElement) extends Component {
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
}

/** Two-state switch. Renders as a `button` with `role="switch"` so it's
  * keyboard-operable (Space/Enter toggles) and screen readers announce the
  * checked state. */
object Toggle extends ComponentFactory[Toggle] {

  val checked = Prop.inOut[Boolean, Toggle](_.checkedVar)

  val disabled = Prop.in[Boolean, Toggle](_.disabledVar)

  private val trackWidth: Length = Length.px(36)
  private val trackHeight: Length = Length.px(20)
  private val thumbSize: Length = Length.px(16)

  override protected def build: Toggle = {
    val root = button(typ := "button")
    val el = new Toggle(root)
    val interact = Interactive.on(root)

    root.amend(
      role := "switch",
      aria.checked <-- el.checkedVar.signal.map(_.toString),
      aria.disabled <-- el.disabledVar.signal,
      Signal
        .combine(el.checkedVar.signal, el.disabledVar.signal, interact.state)
        .styled { case (t, (on, d, i)) =>
          val bg = (on, d) match {
            case (_, true)  => t.surfaceDim
            case (true, _)  => t.brand
            case (false, _) => t.border
          }
          val ring =
            if (i.focused && !i.pressed && !d)
              css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.3).toCss}")
            else css.raw("box-shadow", "none")
          css.position("relative") ++
            css.display(Display.InlineFlex) ++
            css.width(trackWidth) ++
            css.height(trackHeight) ++
            css.borderRadius(radius.pill) ++
            css.background(bg) ++
            css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.transition("background", 150) ++
            css.raw("outline", "none") ++
            css.padding(Length.px(0)) ++
            ring
        },
      span(el.checkedVar.signal.styled { (t, on) =>
        val tx = if (on) trackWidth.toCss + " - " + thumbSize.toCss + " - 2px" else "2px"
        css.position("absolute") ++
          css.top(Length.px(2)) ++
          css.width(thumbSize) ++
          css.height(thumbSize) ++
          css.borderRadius(radius.pill) ++
          css.background(if (on) t.onBrand else t.surface) ++
          css.transition("transform", 150) ++
          css.raw("transform", if (on) s"translateX(calc($tx))" else "translateX(2px)") ++
          css.raw("box-shadow", "0 1px 2px rgba(0,0,0,0.25)")
      }),
      onClick.preventDefault.mapToUnit --> Observer[Unit] { _ =>
        if (!el.disabledVar.now()) el.checkedVar.update(c => !c)
      },
      // Keyboard: Space/Enter on a button already fires click — but role=switch
      // strips the default Enter activation on some screen-reader setups, so
      // bind both keys explicitly to be safe.
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        if (!el.disabledVar.now() && (ev.key == " " || ev.key == "Enter")) {
          ev.preventDefault()
          el.checkedVar.update(c => !c)
        }
      }
    )

    el
  }
}
