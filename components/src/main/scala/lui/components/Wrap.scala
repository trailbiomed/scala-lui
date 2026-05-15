package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object Wrap {
  def apply(gap: Length = spacing.md, align: String = "center")(
      content: Modifier[HtmlElement]*
  ): HtmlElement =
    div(
      css.display(Display.Flex) ++
        css.flexWrap("wrap") ++
        css.alignItems(align) ++
        css.gap(gap),
      content
    )
}
