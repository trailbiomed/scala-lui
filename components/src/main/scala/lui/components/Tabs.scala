package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

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
      role := "tablist",
      el.variantVar.signal.styled { (t, v) =>
        v match {
          case Variant.Underlined =>
            css.display(Display.Flex) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              css.overflowX("auto") ++ css.overflowY("hidden")
          case Variant.Pills =>
            stack.row(Length.px(2)) ++
              css.padding(spacing.md, spacing.xl) ++
              css.background(t.surfaceDim) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              stack.wrap
        }
      },
      // Render tab buttons reactively from the (tabs, active, variant) tuple
      // BUT keep them as a single stable children list so DOM nodes are reused
      // across active-state changes — otherwise clicking a tab replaces the
      // entire button list, losing focus and scrolling the page on overflow.
      children <-- el.tabsVar.signal.map { ts =>
        ts.map { case (id, lbl) => tabButton(id, lbl, el) }.toList
      },
      // Keyboard navigation between tabs. Roving tabindex semantics: only the
      // active tab is in the tab order; arrow keys move focus to siblings and
      // simultaneously activate them.
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        val tabs = root.ref.querySelectorAll("[role='tab']")
        if (tabs.length > 0) {
          val active = dom.document.activeElement
          var idx = -1
          var i = 0
          while (i < tabs.length) {
            if (tabs.item(i) eq active) idx = i
            i += 1
          }
          def activateAt(n: Int): Unit = {
            val clamped = ((n % tabs.length) + tabs.length) % tabs.length
            tabs.item(clamped) match {
              case h: dom.HTMLElement =>
                // preventScroll keeps the page anchor stable when the tab
                // strip overflows horizontally and a sibling tab is off-screen.
                h.asInstanceOf[scala.scalajs.js.Dynamic]
                  .focus(scala.scalajs.js.Dynamic.literal(preventScroll = true))
                h.click()
              case _ => ()
            }
          }
          ev.key match {
            case "ArrowRight" | "ArrowDown" if idx >= 0 =>
              ev.preventDefault()
              activateAt(idx + 1)
            case "ArrowLeft" | "ArrowUp" if idx >= 0 =>
              ev.preventDefault()
              activateAt(idx - 1)
            case "Home" if idx >= 0 =>
              ev.preventDefault()
              activateAt(0)
            case "End" if idx >= 0 =>
              ev.preventDefault()
              activateAt(tabs.length - 1)
            case _ => ()
          }
        }
      }
    )

    el
  }

  private def tabButton(id: String, label: String, el: Tabs): HtmlElement = {
    val btn = button(typ := "button")
    val interact = Interactive.on(btn)
    val isActive: Signal[Boolean] = el.activeVar.signal.map(_ == id)

    btn.amend(
      role := "tab",
      aria.selected <-- isActive,
      // Roving tabindex: active tab gets tabIndex=0; others -1.
      tabIndex <-- isActive.map(if (_) 0 else -1),
      Signal
        .combine(el.variantVar.signal, isActive, interact.state)
        .styled { case (t, (v, act, i)) =>
          v match {
            case Variant.Underlined => underlinedStyle(t, act, i)
            case Variant.Pills      => pillsStyle(t, act, i)
          }
        },
      onClick.preventDefault.mapTo(id) --> el.activeVar.writer,
      label
    )
    btn
  }

  private def underlinedStyle(t: Theme, isActive: Boolean, i: InteractionState): Style = {
    val textColor =
      if (isActive) t.brand
      else if (i.hovered) t.brand
      else t.textMuted
    val borderColor = if (isActive) t.brand else Color.transparent
    val focusRing =
      if (i.focused && !i.pressed)
        css.raw("box-shadow", s"0 0 0 2px ${t.brand.alpha(0.3).toCss}")
      else css.raw("box-shadow", "none")
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
      css.raw("outline", "none") ++
      css.borderRadius(radius.sm) ++
      css.transition("color", 120) ++
      focusRing
  }

  private def pillsStyle(t: Theme, isActive: Boolean, i: InteractionState): Style = {
    val (bg, fg) =
      if (isActive) (t.surface, t.brand)
      else if (i.hovered) (Color.transparent, t.brand)
      else (Color.transparent, t.textMuted)
    val shadow = if (isActive) "0 1px 3px rgba(0,0,0,0.08)" else "none"
    val focusRing =
      if (i.focused && !i.pressed)
        css.raw("box-shadow", s"$shadow, 0 0 0 2px ${t.brand.alpha(0.3).toCss}")
      else css.raw("box-shadow", shadow)
    css.padding(Length.px(4), Length.px(9)) ++
      css.fontSize(fontSizes.md) ++
      css.fontWeight(if (isActive) FontWeight.SemiBold else FontWeight.Medium) ++
      css.color(fg) ++
      css.background(bg) ++
      css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
      css.borderRadius(radius.sm) ++
      css.cursor("pointer") ++
      css.raw("font-family", "inherit") ++
      css.raw("outline", "none") ++
      focusRing ++
      css.transition("color", 120)
  }
}
