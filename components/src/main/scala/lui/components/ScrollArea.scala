package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Scrollable container with optional max-height. Uses native scrollbars (inline styling
  * can't customize webkit-scrollbar pseudo-elements). */
object ScrollArea {
  def apply(
      maxHeight: Length = Length.px(320),
      direction: String = "vertical"
  )(content: Modifier[HtmlElement]*): HtmlElement = {
    val overflowDecl = direction match {
      case "horizontal" => css.overflowX("auto") ++ css.overflowY("hidden")
      case "both"       => css.overflow("auto")
      case _            => css.overflowY("auto") ++ css.overflowX("hidden")
    }
    div(
      themed(t =>
        css.maxHeight(maxHeight) ++
          overflowDecl ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.borderRadius(radius.md) ++
          css.background(t.surface)
      ),
      content
    )
  }
}
