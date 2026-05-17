package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Bare cross/x glyph — the negative counterpart to `Checkmark`. The colour
  * comes from the parent (e.g. set `css.color(t.danger)` on the wrapper). */
object Crossmark {
  def apply(size: Length = Length.px(12)): HtmlElement =
    span(
      css.fontSize(size) ++
        css.fontWeight(FontWeight.Bold) ++
        css.raw("line-height", "1"),
      "✗"
    )
}
