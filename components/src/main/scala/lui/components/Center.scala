package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

object Center {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    div(stack.centerAll ++ css.width(Length.pct(100)), content)

  /** Absolute-centered overlay. Parent must have `position: relative`. */
  def absolute(content: Modifier[HtmlElement]*): HtmlElement =
    div(
      css.position("absolute") ++
        css.raw("top", "50%") ++
        css.raw("left", "50%") ++
        css.raw("transform", "translate(-50%, -50%)"),
      content
    )
}
