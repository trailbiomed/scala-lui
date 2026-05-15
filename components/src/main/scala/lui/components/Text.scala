package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Themed text span. Use `Text.body`, `Text.muted`, `Text.hint`, `Text.label`. The default
  * `Text(...)` is `body`. */
object Text {

  def apply(content: Modifier[HtmlElement]*): HtmlElement = body(content*)

  def body(content: Modifier[HtmlElement]*): HtmlElement =
    span(typo.body, content)

  def muted(content: Modifier[HtmlElement]*): HtmlElement =
    span(typo.muted, content)

  def hint(content: Modifier[HtmlElement]*): HtmlElement =
    span(typo.hint, content)

  def label(content: Modifier[HtmlElement]*): HtmlElement =
    span(typo.label, content)

  def eyebrow(content: Modifier[HtmlElement]*): HtmlElement =
    span(typo.eyebrow, content)
}
