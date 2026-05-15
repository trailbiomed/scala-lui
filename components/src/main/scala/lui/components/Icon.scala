package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Lightweight wrapper around a glyph or SVG. Accepts a string (emoji / unicode) or any
  * child element. Sets `inline-flex` + size + currentColor so callers can change color via
  * the parent's `css.color(...)`. */
object Icon {
  def apply(
      size: Length = Length.px(16),
      color: Option[Color] = None
  )(glyph: Modifier[HtmlElement]*): HtmlElement =
    span(
      css.display(Display.InlineFlex) ++
        css.alignItems("center") ++
        css.justifyContent("center") ++
        css.width(size) ++ css.height(size) ++
        css.fontSize(size) ++
        css.raw("line-height", "1") ++
        color.map(c => css.color(c)).getOrElse(Style.empty),
      glyph
    )
}
