package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object AspectRatio {
  def apply(ratio: Double = 16.0 / 9.0)(content: Modifier[HtmlElement]*): HtmlElement = {
    val pct = 100.0 / ratio
    div(
      css.position("relative") ++
        css.width(Length.pct(100)) ++
        css.raw("padding-bottom", s"$pct%"),
      div(
        css.position("absolute") ++
          css.raw("top", "0") ++ css.raw("left", "0") ++
          css.raw("right", "0") ++ css.raw("bottom", "0"),
        content
      )
    )
  }
}
