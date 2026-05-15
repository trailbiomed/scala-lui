package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Tabs private[components] (val root: HtmlElement) extends Component {
  private[components] val tabsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val activeVar: Var[String] = Var("")
  private[components] val variantVar: Var[Tabs.Variant] = Var(Tabs.Variant.Underlined)
}

object Tabs extends ComponentFactory[Tabs] {

  enum Variant { case Underlined, Pills }

  val tabs = Prop.in[Seq[(String, String)], Tabs](_.tabsVar)

  val active = Prop.inOut[String, Tabs](_.activeVar)

  val variant = Prop.in[Variant, Tabs](_.variantVar)

  override protected def build: Tabs = {
    val root = div()
    val el = new Tabs(root)

    root.amend(
      el.variantVar.signal.styled { (t, v) =>
        v match {
          case Variant.Underlined =>
            css.display(Display.Flex) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              css.overflowX("auto")
          case Variant.Pills =>
            stack.row(Length.px(2)) ++
              css.padding(spacing.md, spacing.xl) ++
              css.background(t.surfaceDim) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              stack.wrap
        }
      },
      children <-- Signal
        .combine(Theme.signal, el.tabsVar.signal, el.activeVar.signal, el.variantVar.signal)
        .map { case (t, ts, act, v) =>
          ts.map((id, lbl) => tabButton(t, id, lbl, act == id, v, el.activeVar)).toList
        }
    )

    el
  }

  private def tabButton(
      t: Theme,
      id: String,
      label: String,
      isActive: Boolean,
      v: Variant,
      activeVar: Var[String]
  ): HtmlElement = {
    val hovered = Var(false)
    button(
      typ := "button",
      hovered.signal.styled { (_, hov) =>
        v match {
          case Variant.Underlined => underlinedStyle(t, isActive, hov)
          case Variant.Pills      => pillsStyle(t, isActive, hov)
        }
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onClick.mapToUnit --> Observer[Unit](_ => activeVar.set(id)),
      label
    )
  }

  private def underlinedStyle(t: Theme, isActive: Boolean, hovered: Boolean): Style = {
    val textColor =
      if (isActive) t.brand
      else if (hovered) t.brand
      else t.textMuted
    val borderColor = if (isActive) t.brand else Color.transparent
    css.padding(spacing.md, spacing.xl) ++
      css.fontSize(fontSizes.lg) ++
      css.fontWeight(if (isActive) FontWeight.SemiBold else FontWeight.Medium) ++
      css.color(textColor) ++
      css.background(Color.transparent) ++
      css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
      css.borderBottom(Length.px(2.5), BorderStyle.Solid, borderColor) ++
      css.cursor("pointer") ++
      css.raw("font-family", "inherit") ++
      css.raw("margin-bottom", "-2px") ++
      css.raw("white-space", "nowrap") ++
      css.transition("color", 120)
  }

  private def pillsStyle(t: Theme, isActive: Boolean, hovered: Boolean): Style = {
    val (bg, fg) =
      if (isActive) (t.surface, t.brand)
      else if (hovered) (Color.transparent, t.brand)
      else (Color.transparent, t.textMuted)
    val shadow = if (isActive) "0 1px 3px rgba(0,0,0,0.08)" else "none"
    css.padding(Length.px(4), Length.px(9)) ++
      css.fontSize(fontSizes.md) ++
      css.fontWeight(if (isActive) FontWeight.SemiBold else FontWeight.Medium) ++
      css.color(fg) ++
      css.background(bg) ++
      css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
      css.borderRadius(radius.sm) ++
      css.cursor("pointer") ++
      css.raw("font-family", "inherit") ++
      css.raw("box-shadow", shadow) ++
      css.transition("color", 120)
  }
}
