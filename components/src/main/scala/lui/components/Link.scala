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

  enum Variant { case Brand, Muted, Plain }

  val href = Prop.in[String, Link](_.hrefVar)
  val external = Prop.in[Boolean, Link](_.externalVar)
  val variant = Prop.in[Variant, Link](_.variantVar)

  def children(content: Modifier[HtmlElement]*): Mod[Link] = el => el.root.amend(content*)

  override protected def build: Link = {
    val root = a()
    val el = new Link(root)

    root.amend(
      Signal.combine(el.variantVar.signal, el.interact.state).styled { case (t, (v, i)) =>
        val color = v match {
          case Variant.Brand => if (i.hovered) t.brandHover else t.brand
          case Variant.Muted => if (i.hovered) t.text else t.textMuted
          case Variant.Plain => t.text
        }
        css.color(color) ++
          css.raw("text-decoration", if (i.hovered) "underline" else "none") ++
          css.cursor("pointer") ++
          css.transition("color", 120)
      },
      htmlHref <-- el.hrefVar.signal,
      target <-- el.externalVar.signal.map(if (_) "_blank" else "_self"),
      rel <-- el.externalVar.signal.map(if (_) "noopener noreferrer" else "")
    )

    el
  }
}
