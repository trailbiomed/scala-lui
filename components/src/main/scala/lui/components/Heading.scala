package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Semantic heading with a level (1-4). Level picks the HTML tag AND the type scale. */
object Heading {
  def apply(level: Int = 1)(content: Modifier[HtmlElement]*): HtmlElement = {
    val style = level match {
      case 1 => typo.h1
      case 2 => typo.h2
      case 3 =>
        ThemedStyle(t =>
          css.fontSize(fontSizes.xxl) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.color(t.text)
        )
      case _ =>
        ThemedStyle(t =>
          css.fontSize(fontSizes.xl) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.color(t.text)
        )
    }
    val resetMargin = css.margin(Length.px(0))
    val composed = style ++ resetMargin
    level match {
      case 1 => h1(composed, content)
      case 2 => h2(composed, content)
      case 3 => h3(composed, content)
      case _ => h4(composed, content)
    }
  }
}
