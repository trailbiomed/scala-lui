package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Bare checkmark glyph. Pair with a parent that sets the color (e.g. inside `Checkbox`). */
object Checkmark {
  def apply(size: Length = Length.px(12)): HtmlElement =
    span(
      css.fontSize(size) ++
        css.fontWeight(FontWeight.Bold) ++
        css.raw("line-height", "1"),
      "✓"
    )
}
