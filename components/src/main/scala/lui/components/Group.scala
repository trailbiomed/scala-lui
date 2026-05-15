package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** A row of attached controls. Renders children with `gap: 0` so they butt up against each
  * other; pair with components that already share a border style for an "attached" look.
  * Use `Wrap` instead if you want gaps. */
object Group {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      css.display(Display.InlineFlex) ++
        css.alignItems("stretch") ++
        css.gap(Length.zero),
      content
    )
}
