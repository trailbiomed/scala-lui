package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Code private[components] (val root: HtmlElement) extends Component {
  private[components] val textVar: Var[String] = Var("")
  private[components] val blockVar: Var[Boolean] = Var(false)
  private[components] val variantVar: Var[Code.Variant] = Var(Code.Variant.Boxed)
}

/** Inline (default) or block-formatted code. Set `block := true` for a multi-line
  * preformatted panel; defaults to a single-line inline tag.
  *
  * `variant` controls the surface treatment:
  *  - `Boxed` (default) — 1px `t.border` border with `radius.md` (block) or
  *    `radius.sm` (inline). Reads as a distinct panel.
  *  - `Tinted` — no border, tinted `t.surfaceDim` background, small radius
  *    (`radius.sm`). Reads as part of the surrounding text flow.
  *
  * The `Tinted` variant also uses `1em` font-size so it inherits from its
  * parent — handy in contexts (slides, hero copy) where the surrounding
  * text is scaled up. `Boxed` keeps the token-sized `fontSizes.md` typical
  * of body copy.
  */
object Code extends ComponentFactory[Code] {

  enum Variant { case Boxed, Tinted }

  val text = Prop.in[String, Code](_.textVar)
  val block = Prop.in[Boolean, Code](_.blockVar)
  val variant = Prop.in[Variant, Code](_.variantVar)

  override protected def build: Code = {
    val root = pre()
    val el = new Code(root)

    root.amend(
      Signal.combine(el.blockVar.signal, el.variantVar.signal).styled { case (t, (isBlock, v)) =>
        val (bg, border, size) = v match {
          case Variant.Boxed  =>
            (
              t.surfaceDim,
              css.border(Length.px(1), BorderStyle.Solid, t.border),
              css.fontSize(fontSizes.md)
            )
          case Variant.Tinted =>
            (
              t.surfaceDim,
              Style.empty,
              css.raw("font-size", "1em")
            )
        }

        val base =
          css.color(t.text) ++
            css.background(bg) ++
            border ++
            css.raw(
              "font-family",
              "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace"
            ) ++
            size ++
            css.raw("margin", "0")

        if (isBlock)
          base ++
            css.display(Display.Block) ++
            css.padding(spacing.lg, spacing.xl) ++
            css.borderRadius(v match {
              case Variant.Boxed  => radius.md
              case Variant.Tinted => radius.sm
            }) ++
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
