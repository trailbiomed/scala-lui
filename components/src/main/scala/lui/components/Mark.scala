package lui.components

import com.raquo.laminar.api.L.{Mod as _, mark as markTag, *}
import lui.style.*

object Mark {
  def apply(content: Modifier[HtmlElement]*): HtmlElement =
    markTag(
      themed(t =>
        css.background(t.warningSoft) ++
          css.color(t.text) ++
          css.padding(Length.px(0), Length.px(2)) ++
          css.borderRadius(Length.px(2))
      ),
      content
    )
}
