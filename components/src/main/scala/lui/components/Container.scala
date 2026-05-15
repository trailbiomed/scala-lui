package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object Container {
  def apply(
      maxWidth: Length = Length.px(1100),
      pad: Length = spacing.xl
  )(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      css.maxWidth(maxWidth) ++
        css.padding(pad, pad) ++
        css.raw("margin", "0 auto") ++
        css.width(Length.pct(100)) ++
        css.raw("box-sizing", "border-box"),
      content
    )
}
