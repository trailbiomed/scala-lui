package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** A small filled tile previewing a color. Useful in palettes and color pickers. */
object ColorSwatch {
  def apply(c: Color, size: Length = Length.px(20), rounded: Boolean = true): HtmlElement =
    span(
      themed(t =>
        css.width(size) ++ css.height(size) ++
          css.background(c) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.borderRadius(if (rounded) radius.sm else Length.px(0)) ++
          css.raw("display", "inline-block")
      )
    )
}
