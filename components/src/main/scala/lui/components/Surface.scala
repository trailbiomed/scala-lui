package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

/** Ad-hoc themed surfaces. Use these for one-off clickable cards / panels in views where a
  * full dedicated `Component` would be overkill. Each returns a plain `HtmlElement`. */
object Surface {

  /** A clickable themed surface that highlights its border on hover. The same shape used by
    * `Card(interactive := true)` and `ReferenceCard` — but without their fixed
    * inner layouts. Pass `extra` to override the default symmetric padding / radius. */
  def interactive(
      pad: Length = spacing.lg,
      rad: Length = radius.lg,
      click: Sink[Unit] = Observer.empty,
      extra: Style = Style.empty
  )(content: Modifier[HtmlElement]*): HtmlElement = {
    val root = div()
    val interact = Interactive.on(root)
    root.amend(
      interact.state.styled { (t, i) =>
        val bd = if (i.hovered) t.borderActive else t.border
        css.background(t.surface) ++
          css.borderRadius(rad) ++
          css.padding(pad) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.cursor("pointer") ++
          css.transition("border-color", 150) ++
          extra
      },
      onClick.mapToUnit --> click,
      content
    )
    root
  }
}
