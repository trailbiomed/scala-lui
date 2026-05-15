package lui.components

import com.raquo.laminar.api.L.{Mod as _, em as emTag, *}
import lui.style.*

object Em {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    emTag(
      typo.body ++ css.raw("font-style", "italic"),
      content
    )
}
