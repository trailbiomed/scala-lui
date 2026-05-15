package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Sticky bottom-of-viewport bar for primary actions. Position `fixed`, full width. */
object ActionBar {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      themed(t =>
        css.position("fixed") ++
          css.raw("bottom", "0") ++
          css.raw("left", "0") ++
          css.raw("right", "0") ++
          css.background(t.surface) ++
          css.raw("border-top", s"1px solid ${t.border.toCss}") ++
          css.padding(spacing.lg, spacing.xl) ++
          css.zIndex(20) ++
          stack.row(spacing.md) ++
          css.justifyContent("flex-end") ++
          css.raw("box-shadow", "0 -4px 16px rgba(0,0,0,0.06)")
      ),
      content
    )
}
