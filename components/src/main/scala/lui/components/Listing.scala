package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Bulleted or numbered list. Named `Listing` to avoid shadowing `scala.List`. */
object Listing {
  enum Style { case Bulleted, Numbered, None }

  def apply(
      style: Style = Style.Bulleted,
      gap: Length = spacing.sm
  )(items: HtmlElement*): HtmlElement = {
    val bullet = style match {
      case Style.Bulleted => "disc"
      case Style.Numbered => "decimal"
      case Style.None     => "none"
    }
    val baseStyle = typo.body ++
      css.raw("padding-left", spacing.xl.toCss) ++
      css.raw("margin", "0") ++
      css.raw("list-style", bullet) ++
      css.display(Display.Flex) ++
      css.flexDirection("column") ++
      css.gap(gap)
    val el = if (style == Style.Numbered) ol(baseStyle) else ul(baseStyle)
    items.foreach(it => el.amend(it))
    el
  }

  def item(content: Modifier[HtmlElement]*): HtmlElement = li(content)
}
