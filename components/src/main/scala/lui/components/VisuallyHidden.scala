package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Hides content from sighted users while keeping it discoverable by screen readers. */
object VisuallyHidden {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    span(
      css.position("absolute") ++
        css.width(Length.px(1)) ++
        css.height(Length.px(1)) ++
        css.padding(Length.zero) ++
        css.raw("margin", "-1px") ++
        css.overflow("hidden") ++
        css.raw("clip", "rect(0,0,0,0)") ++
        css.raw("white-space", "nowrap") ++
        css.raw("border", "0"),
      content
    )
}
