package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Code private[components] (val root: HtmlElement) extends Component {
  private[components] val textVar: Var[String] = Var("")
  private[components] val blockVar: Var[Boolean] = Var(false)
}

/** Inline (default) or block-formatted code. Set `block := true` for a multi-line
  * preformatted panel; defaults to a single-line inline tag. */
object Code extends ComponentFactory[Code] {

  val text = Prop.in[String, Code](_.textVar)
  val block = Prop.in[Boolean, Code](_.blockVar)

  override protected def build: Code = {
    val root = pre()
    val el = new Code(root)

    root.amend(
      el.blockVar.signal.styled { (t, isBlock) =>
        val base =
          css.color(t.text) ++
            css.background(t.surfaceDim) ++
            css.border(Length.px(1), BorderStyle.Solid, t.border) ++
            css.raw(
              "font-family",
              "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace"
            ) ++
            css.fontSize(fontSizes.md) ++
            css.raw("margin", "0")

        if (isBlock)
          base ++
            css.display(Display.Block) ++
            css.padding(spacing.lg, spacing.xl) ++
            css.borderRadius(radius.md) ++
            css.lineHeight(1.55) ++
            css.raw("white-space", "pre") ++
            css.overflowX("auto")
        else
          base ++
            css.display(Display.InlineFlex) ++
            css.padding(Length.px(1), Length.px(6)) ++
            css.borderRadius(radius.sm) ++
            css.raw("white-space", "nowrap")
      },
      child.text <-- el.textVar.signal
    )
    el
  }
}
