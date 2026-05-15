package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Filled dot used by radio inputs. */
object Radiomark {
  def apply(size: Length = Length.px(8)): HtmlElement =
    span(
      themed(t =>
        css.width(size) ++
          css.height(size) ++
          css.borderRadius(radius.pill) ++
          css.background(t.brand) ++
          css.raw("display", "inline-block")
      )
    )
}
