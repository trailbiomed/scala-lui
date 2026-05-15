package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object SimpleGrid {
  /** CSS Grid with N equal-width columns. */
  def apply(columns: Int = 3, gap: Length = spacing.lg)(
      content: Modifier[HtmlElement]*
  ): HtmlElement =
    div(
      css.raw("display", "grid") ++
        css.raw("grid-template-columns", s"repeat($columns, minmax(0, 1fr))") ++
        css.gap(gap),
      content
    )

  /** Auto-fit columns of at least `minChildWidth`. */
  def autoFit(minChildWidth: Length, gap: Length = spacing.lg)(
      content: Modifier[HtmlElement]*
  ): HtmlElement =
    div(
      css.raw("display", "grid") ++
        css.raw(
          "grid-template-columns",
          s"repeat(auto-fit, minmax(${minChildWidth.toCss}, 1fr))"
        ) ++
        css.gap(gap),
      content
    )
}
