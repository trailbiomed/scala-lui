package lui.components

import com.raquo.laminar.api.L.{Mod as _, href as htmlHref, *}
import lui.*
import lui.style.*

final class Link private[components] (val root: HtmlElement) extends Component {
  private[components] val hrefVar: Var[String] = Var("#")
  private[components] val externalVar: Var[Boolean] = Var(false)
  private[components] val variantVar: Var[Link.Variant] = Var(Link.Variant.Brand)
}

object Link extends ComponentFactory[Link] {

  /** `Brand` / `Muted` / `Plain` are flowing inline text links. `Chip` styles the link as
    * a small brand-tinted pill (e.g. for inline citations, footnote markers). Wrap a
    * `Chip` Link in `sup(...)` if you want superscript citation positioning. */
  enum Variant { case Brand, Muted, Plain, Chip }

  val href = Prop.in[String, Link](_.hrefVar)
  val external = Prop.in[Boolean, Link](_.externalVar)
  val variant = Prop.in[Variant, Link](_.variantVar)

  def children(content: Modifier[HtmlElement]*): Mod[Link] = el => el.root.amend(content*)

  override protected def build: Link = {
    val root = a()
    val el = new Link(root)

    root.amend(
      Signal.combine(el.variantVar.signal, el.interact.state).styled { case (t, (v, i)) =>
        v match {
          case Variant.Chip => chipStyle(t, i)
          case _            => textLinkStyle(t, v, i)
        }
      },
      htmlHref <-- el.hrefVar.signal,
      target <-- el.externalVar.signal.map(if (_) "_blank" else "_self"),
      rel <-- el.externalVar.signal.map(if (_) "noopener noreferrer" else "")
    )

    el
  }

  private def textLinkStyle(t: Theme, v: Variant, i: InteractionState): Style = {
    val color = v match {
      case Variant.Brand => if (i.hovered) t.brandHover else t.brand
      case Variant.Muted => if (i.hovered) t.text else t.textMuted
      case Variant.Plain => t.text
      case Variant.Chip  => t.brand // unreachable in practice
    }
    css.color(color) ++
      css.raw("text-decoration", if (i.hovered) "underline" else "none") ++
      css.cursor("pointer") ++
      css.transition("color", 120)
  }

  private def chipStyle(t: Theme, i: InteractionState): Style = {
    val bg = if (i.hovered) t.brand.alpha(0.28) else t.brand.alpha(0.18)
    css.display(Display.InlineFlex) ++
      css.alignItems("center") ++ css.justifyContent("center") ++
      css.minWidth(Length.px(18)) ++
      css.padding(Length.px(0), Length.px(6)) ++
      css.background(bg) ++
      css.color(t.brand) ++
      css.borderRadius(radius.sm) ++
      css.fontSize(fontSizes.xs) ++
      css.fontWeight(FontWeight.SemiBold) ++
      css.raw("line-height", "1.4") ++
      css.cursor("pointer") ++
      css.raw("text-decoration", "none") ++
      css.transition("background", 120)
  }
}
