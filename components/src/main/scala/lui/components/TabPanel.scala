package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class TabPanel private[components] (
    val root: HtmlElement,
    private[components] val activeVar: Var[String],
    private[components] val variantVar: Var[Tabs.Variant],
    private[components] val layoutVar: Var[TabPanel.Layout],
    private[components] val panelsVar: Var[Vector[TabPanel.PanelDecl]]
) extends Component

/** Tabs strip + bodies in one piece, with layout-stable rendering by default
  * so clicking between tabs of different heights doesn't make the page jump.
  *
  * Each `panel(key, label, content)` declares one body. The strip is built
  * automatically from the declared panel labels; the first declared panel is
  * the default active one. */
object TabPanel extends ComponentFactory[TabPanel] {

  enum Layout {
    case Stable
    case Swap
  }

  final case class PanelDecl(
      key: String,
      label: String,
      content: HtmlElement,
      disabled: Boolean = false,
      hidden: Boolean = false
  )

  val active = Prop.inOut[String, TabPanel](_.activeVar)
  val variant = Prop.in[Tabs.Variant, TabPanel](_.variantVar)
  val layout = Prop.in[Layout, TabPanel](_.layoutVar)

  def panel(
      key: String,
      label: String,
      content: => HtmlElement,
      disabled: Boolean = false,
      hidden: Boolean = false
  ): Mod[TabPanel] = el => {
    if (!hidden) {
      el.panelsVar.update(_ :+ PanelDecl(key, label, content, disabled, hidden))
    }
  }

  override protected def build: TabPanel = {
    val activeVar = Var("")
    val variantVar = Var[Tabs.Variant](Tabs.Variant.Underlined)
    val layoutVar = Var[Layout](Layout.Stable)
    val panelsVar = Var[Vector[PanelDecl]](Vector.empty)

    val visiblePanels: Signal[Vector[PanelDecl]] =
      panelsVar.signal.map(_.filterNot(_.disabled))

    val strip: HtmlElement = div()
    strip.amend(
      role := "tablist",
      variantVar.signal.styled { (t, v) =>
        v match {
          case Tabs.Variant.Underlined =>
            css.display(Display.Flex) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              css.overflowX("auto") ++ css.overflowY("hidden")
          case Tabs.Variant.Pills =>
            stack.row(Length.px(2)) ++
              css.padding(spacing.md, spacing.xl) ++
              css.background(t.surfaceDim) ++
              css.borderBottom(Length.px(1.5), BorderStyle.Solid, t.border) ++
              stack.wrap
        }
      },
      children <-- visiblePanels.map { ps =>
        ps.iterator.map(p => tabButton(p, activeVar, variantVar)).toList
      },
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        val tabsEls = strip.ref.querySelectorAll("[role='tab']")
        if (tabsEls.length > 0) {
          val active = dom.document.activeElement
          var idx = -1
          var i = 0
          while (i < tabsEls.length) {
            if (tabsEls.item(i) eq active) idx = i
            i += 1
          }
          def activateAt(n: Int): Unit = {
            val clamped = ((n % tabsEls.length) + tabsEls.length) % tabsEls.length
            tabsEls.item(clamped) match {
              case h: dom.HTMLElement =>
                val _ = h.asInstanceOf[scala.scalajs.js.Dynamic]
                  .focus(scala.scalajs.js.Dynamic.literal(preventScroll = true))
                h.click()
              case _ => ()
            }
          }
          ev.key match {
            case "ArrowRight" | "ArrowDown" if idx >= 0 => ev.preventDefault(); activateAt(idx + 1)
            case "ArrowLeft"  | "ArrowUp"   if idx >= 0 => ev.preventDefault(); activateAt(idx - 1)
            case "Home"                     if idx >= 0 => ev.preventDefault(); activateAt(0)
            case "End"                      if idx >= 0 => ev.preventDefault(); activateAt(tabsEls.length - 1)
            case _ => ()
          }
        }
      }
    )

    val panelsContainer = div(
      layoutVar.signal.styled { (_, lay) =>
        lay match {
          case Layout.Stable =>
            css.display(Display.Grid) ++ css.gridTemplateColumns("minmax(0, 1fr)")
          case Layout.Swap =>
            css.display(Display.Block)
        }
      },
      children <-- Signal
        .combine(layoutVar.signal, panelsVar.signal, activeVar.signal)
        .map { case (lay, panels, activeKey) =>
          lay match {
            case Layout.Stable =>
              panels.iterator.map { p =>
                val isActive = p.key == activeKey
                val visibilityDecls =
                  if (isActive) css.raw("visibility", "visible") ++ css.pointerEvents("auto")
                  else css.raw("visibility", "hidden") ++ css.pointerEvents("none")
                div(
                  role := "tabpanel",
                  aria.label := p.label,
                  css.gridColumn("1") ++ css.gridRow("1") ++ visibilityDecls,
                  p.content
                )
              }.toList
            case Layout.Swap =>
              panels.find(_.key == activeKey) match {
                case Some(p) => List(div(role := "tabpanel", aria.label := p.label, p.content))
                case None    => Nil
              }
          }
        }
    )

    val root = div(
      stack.col(spacing.md),
      panelsVar.signal.changes
        .withCurrentValueOf(activeVar.signal)
        .collect {
          case (ps, cur) if cur.isEmpty || !ps.exists(_.key == cur) =>
            ps.iterator.filterNot(_.disabled).map(_.key).nextOption().getOrElse("")
        }
        .filter(_.nonEmpty)
        --> activeVar.writer,
      strip,
      panelsContainer
    )

    new TabPanel(root, activeVar, variantVar, layoutVar, panelsVar)
  }

  private def tabButton(
      p: PanelDecl,
      activeVar: Var[String],
      variantVar: Var[Tabs.Variant]
  ): HtmlElement = {
    val btn = button(typ := "button")
    val interact = Interactive.on(btn)
    val isActive: Signal[Boolean] = activeVar.signal.map(_ == p.key)
    btn.amend(
      role := "tab",
      aria.selected <-- isActive,
      tabIndex <-- isActive.map(if (_) 0 else -1),
      Signal.combine(variantVar.signal, isActive, interact.state).styled { case (t, (v, act, i)) =>
        v match {
          case Tabs.Variant.Underlined => underlinedStyle(t, act, i)
          case Tabs.Variant.Pills      => pillsStyle(t, act, i)
        }
      },
      onClick.preventDefault.mapTo(p.key) --> activeVar.writer,
      p.label
    )
    btn
  }

  private def underlinedStyle(t: Theme, isActive: Boolean, i: InteractionState): Style = {
    val textColor =
      if (isActive) t.brand
      else if (i.hovered) t.brand
      else t.textMuted
    val borderColor =
      if (isActive) t.brand
      else if (i.focused && !i.pressed) t.brand.alpha(0.5)
      else Color.transparent
    val bg =
      if (i.focused && !i.pressed) t.brandSoft
      else Color.transparent
    css.padding(spacing.md, spacing.xl) ++
      css.fontSize(fontSizes.lg) ++
      css.fontWeight(if (isActive) FontWeight.SemiBold else FontWeight.Medium) ++
      css.color(textColor) ++
      css.background(bg) ++
      css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
      css.borderBottom(Length.px(2.5), BorderStyle.Solid, borderColor) ++
      css.cursor("pointer") ++
      css.raw("font-family", "inherit") ++
      css.raw("margin-bottom", "-2px") ++
      css.raw("white-space", "nowrap") ++
      css.raw("outline", "none") ++
      css.transition("color", 120) ++ css.transition("border-color", 120) ++
      css.transition("background", 120)
  }

  private def pillsStyle(t: Theme, isActive: Boolean, i: InteractionState): Style = {
    val (bg, fg) =
      if (isActive && i.focused && !i.pressed) (t.brandSoft, t.brand)
      else if (isActive)                       (t.surface,   t.brand)
      else if (i.focused && !i.pressed)        (t.brandSoft, t.brand)
      else if (i.hovered)                      (Color.transparent, t.brand)
      else                                     (Color.transparent, t.textMuted)
    val shadow =
      if (isActive && !(i.focused && !i.pressed)) "0 1px 3px rgba(0,0,0,0.08)"
      else "none"
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
      css.raw("box-shadow", shadow) ++
      css.transition("color", 120) ++ css.transition("background", 120)
  }
}
