package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

/** Ad-hoc themed surfaces. Use these for one-off clickable cards / panels in views where a
  * full dedicated `Component` would be overkill. Each returns a plain `HtmlElement`. */
object Surface {

  /** A clickable themed surface that highlights its border on hover. The same shape used by
    * `Card(interactive := true)` and `ReferenceCard` — but without their fixed
    * inner layouts. Pass `extra` to override the default symmetric padding / radius.
    *
    * Renders as a `<button>` so it's keyboard-focusable and Space/Enter
    * activate it. The focus ring is the standard 3-pixel brand alpha-ring. */
  def interactive(
      pad: Length = spacing.lg,
      rad: Length = radius.lg,
      click: Sink[Unit] = Observer.empty,
      extra: Style = Style.empty
  )(content: Modifier[HtmlElement]*): HtmlElement = {
    val root = button(typ := "button")
    val interact = Interactive.on(root)
    root.amend(
      interact.state.styled { (t, i) =>
        val bd = if (i.hovered) t.borderActive else t.border
        val ring =
          if (i.focused && !i.pressed)
            css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.25).toCss}")
          else css.raw("box-shadow", "none")
        css.background(t.surface) ++
          css.color(t.text) ++
          css.borderRadius(rad) ++
          css.padding(pad) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.cursor("pointer") ++
          css.raw("font-family", "inherit") ++
          css.raw("text-align", "left") ++
          css.raw("outline", "none") ++
          css.width(Length.pct(100)) ++
          css.transition("border-color", 150) ++
          extra ++
          ring
      },
      onClick.preventDefault.mapToUnit --> click,
      content
    )
    root
  }
}
