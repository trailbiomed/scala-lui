package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object Blockquote {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    blockQuote(
      themed(t =>
        css.raw("margin", "0") ++
          css.padding(spacing.lg, spacing.xl) ++
          css.raw("border-left", s"3px solid ${t.borderActive.toCss}") ++
          css.color(t.textMuted) ++
          css.fontSize(fontSizes.lg) ++
          css.raw("font-style", "italic")
      ),
      content
    )
}
