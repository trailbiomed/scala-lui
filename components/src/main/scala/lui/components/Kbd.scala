package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Kbd private[components] (val root: HtmlElement) extends Component {
  private[components] val keyVar: Var[String] = Var("")
}

/** Renders a keyboard key glyph (e.g. `"⌘"`, `"K"`, `"Enter"`). For chords, compose multiple
  * `Kbd` instances with a separator: `Kbd("⌘") + " " + Kbd("K")`. */
object Kbd extends ComponentFactory[Kbd] {

  val key = Prop.in[String, Kbd](_.keyVar)

  override protected def build: Kbd = {
    val root = span()
    val el = new Kbd(root)

    root.amend(
      themed(t =>
        css.display(Display.InlineFlex) ++
          css.alignItems("center") ++ css.justifyContent("center") ++
          css.minWidth(Length.px(20)) ++
          css.padding(Length.px(1), Length.px(6)) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.sm) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.color(t.text) ++
          css.background(t.surface) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.raw("box-shadow", s"0 1px 0 0 ${t.border.toCss}") ++
          css.raw(
            "font-family",
            "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace"
          ) ++
          css.raw("line-height", "1.4") ++
          css.raw("white-space", "nowrap")
      ),
      child.text <-- el.keyVar.signal
    )
    el
  }
}
