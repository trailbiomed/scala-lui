package lui.components

import com.raquo.laminar.api.L.{Mod as _, href as htmlHref, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Link private[components] (val root: HtmlElement) extends Component {
  private[components] val hrefVar: Var[String] = Var("#")
  private[components] val externalVar: Var[Boolean] = Var(false)
  private[components] val variantVar: Var[Link.Variant] = Var(Link.Variant.Brand)
  private[components] val scrollTargetVar: Var[String] = Var("")
  private[components] val scrolledBus: EventBus[String] = new EventBus[String]
}

object Link extends ComponentFactory[Link] {

  /** `Brand` / `Muted` / `Plain` are flowing inline text links. `Chip` styles the link as
    * a small brand-tinted pill (e.g. for inline citations, footnote markers). Wrap a
    * `Chip` Link in `sup(...)` if you want superscript citation positioning. */
  enum Variant { case Brand, Muted, Plain, Chip }

  val href = Prop.in[String, Link](_.hrefVar)
  val external = Prop.in[Boolean, Link](_.externalVar)
  val variant = Prop.in[Variant, Link](_.variantVar)

  /** Element id to scroll into view on click, without updating the URL hash.
    *
    * When set (non-empty), the link sets its `href` to `"#$id"` (so right-click
    * → copy link and keyboard activation still work), `preventDefault`s the
    * click, and calls `scrollIntoView` on the matching element after the
    * Airstream transaction flushes. This avoids the classic
    * fragment-navigation pitfall where the browser's scroll-anchor accounting
    * later resets `scrollY` to 0 if the target moves under a hidden subtree.
    *
    * The `scrolled` Out fires the id after each successful scroll — useful for
    * reactive consumers (e.g. activate the tab that contains the target). */
  val scrollTarget = Prop.in[String, Link](_.scrollTargetVar)

  /** Emits the scrollTarget id every time a scroll-link is activated. */
  val scrolled = Prop.out[String, Link](_.scrolledBus)

  def children(content: Modifier[HtmlElement]*): Mod[Link] = el => el.root.amend(content*)

  override protected def build: Link = {
    val root = a()
    val el = new Link(root)

    root.amend(
      Signal.combine(el.variantVar.signal, el.interact.state).styled { case (t, (v, i)) =>
        v match {
          case Variant.Chip  => chipStyle(t, i)
          case Variant.Brand => textLinkStyle(if (i.hovered) t.brandHover else t.brand, i)
          case Variant.Muted => textLinkStyle(if (i.hovered) t.text else t.textMuted, i)
          case Variant.Plain => textLinkStyle(t.text, i)
        }
      },
      // When scrollTarget is set, that wins for href (so the keyboard target
      // is the in-page anchor). Otherwise the explicit href prop drives it.
      htmlHref <-- Signal.combine(el.hrefVar.signal, el.scrollTargetVar.signal).map {
        case (_, target) if target.nonEmpty => s"#$target"
        case (h, _)                         => h
      },
      target <-- el.externalVar.signal.map(if (_) "_blank" else "_self"),
      rel <-- el.externalVar.signal.map(if (_) "noopener noreferrer" else ""),
      // Click handler — intercept ONLY when scrollTarget is set. Plain hrefs
      // keep their default navigation behaviour.
      onClick --> Observer[dom.MouseEvent] { ev =>
        val id = el.scrollTargetVar.now()
        if (id.nonEmpty) {
          ev.preventDefault()
          // Defer one microtask so any concurrent tab/route swap that reveals
          // the target has had a chance to apply.
          val _ = scala.scalajs.js.timers.setTimeout(0) {
            val node = dom.document.getElementById(id)
            if (node != null) {
              node.asInstanceOf[scala.scalajs.js.Dynamic]
                .scrollIntoView(scala.scalajs.js.Dynamic.literal(
                  behavior = "smooth",
                  block = "start"
                ))
              el.scrolledBus.writer.onNext(id)
            }
          }
        }
      }
    )

    el
  }

  private def textLinkStyle(fallback: Color, i: InteractionState): Style =
    fg.color(fallback) ++
      css.raw("text-decoration", if (i.hovered) "underline" else "none") ++
      css.cursor("pointer") ++
      css.transition("color", 120)

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
