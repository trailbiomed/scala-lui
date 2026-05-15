package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object Bleed {
  def apply(
      inline: Length = Length.zero,
      block: Length = Length.zero
  )(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      css.raw("margin-left", s"-${inline.toCss}") ++
        css.raw("margin-right", s"-${inline.toCss}") ++
        css.raw("margin-top", s"-${block.toCss}") ++
        css.raw("margin-bottom", s"-${block.toCss}"),
      content
    )
}
