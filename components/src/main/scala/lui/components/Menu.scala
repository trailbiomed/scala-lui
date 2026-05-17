package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Menu private[components] (
    val root: HtmlElement,
    private[components] val popover: Popover
) extends Component {
  private[components] val itemsVar: Var[Seq[Menu.Item]] = Var(Seq.empty)
  private[components] val selectBus: EventBus[String] = new EventBus[String]
}

/** Click-toggled action menu, built on `Popover`. Items can be operated by
  * keyboard: ArrowDown/ArrowUp navigate, Enter/Space activates, Escape closes
  * and restores focus to the trigger. */
object Menu extends ComponentFactory[Menu] {

  final case class Item(key: String, label: String, icon: String = "", danger: Boolean = false)

  val items = Prop.in[Seq[Item], Menu](_.itemsVar)
  val select = Prop.out[String, Menu](_.selectBus)

  def trigger(content: Modifier[HtmlElement]*): Mod[Menu] = el =>
    Popover.trigger(content*)(el.popover)

  override protected def build: Menu = {
    val popover = Popover(Popover.bodyRole := "menu")
    val el = new Menu(popover.root, popover)

    Popover.body(
      div(
        stack.col(Length.zero),
        children <-- el.itemsVar.signal.map { its =>
          its.map(i => itemEl(i, el)).toList
        },
        onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
          val container = popover.bodySlot.ref
          val items = container.querySelectorAll("[role='menuitem']")
          if (items.length > 0) {
            val active = dom.document.activeElement
            var idx = -1
            var i = 0
            while (i < items.length) {
              if (items.item(i) eq active) idx = i
              i += 1
            }
            def focusAt(n: Int): Unit = {
              val clamped = ((n % items.length) + items.length) % items.length
              items.item(clamped) match {
                case h: dom.HTMLElement => h.focus()
                case _                  => ()
              }
            }
            ev.key match {
              case "ArrowDown" =>
                ev.preventDefault()
                focusAt(if (idx < 0) 0 else idx + 1)
              case "ArrowUp" =>
                ev.preventDefault()
                focusAt(if (idx < 0) items.length - 1 else idx - 1)
              case "Home" =>
                ev.preventDefault()
                focusAt(0)
              case "End" =>
                ev.preventDefault()
                focusAt(items.length - 1)
              case _ => ()
            }
          }
        }
      )
    )(popover)

    // When the menu opens, focus the first item so arrow keys land somewhere.
    popover.root.amend(
      popover.openVar.signal.changes.filter(identity) --> Observer[Boolean] { _ =>
        val _ = scala.scalajs.js.timers.setTimeout(0) {
          val items = popover.bodySlot.ref.querySelectorAll("[role='menuitem']")
          if (items.length > 0) {
            items.item(0) match {
              case h: dom.HTMLElement => h.focus()
              case _                  => ()
            }
          }
        }
      }
    )

    el
  }

  private def itemEl(it: Item, menu: Menu): HtmlElement = {
    val root = button(typ := "button")
    val interact = Interactive.on(root)
    root.amend(
      role := "menuitem",
      interact.state.styled { (t, i) =>
        val fg = if (it.danger) t.danger else t.text
        val bg = if (i.hovered || i.focused) t.surfaceDim else Color.transparent
        val ring =
          if (i.focused && !i.pressed)
            css.raw("box-shadow", s"0 0 0 2px ${t.brand.alpha(0.35).toCss}")
          else css.raw("box-shadow", "none")
        stack.row(spacing.sm) ++
          css.padding(Length.px(6), spacing.md) ++
          css.cursor("pointer") ++
          css.color(fg) ++
          css.background(bg) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.lg) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.raw("font-family", "inherit") ++
          css.raw("text-align", "left") ++
          css.raw("outline", "none") ++
          css.width(Length.pct(100)) ++
          ring
      },
      if (it.icon.nonEmpty) span(it.icon) else emptyNode,
      span(it.label),
      onClick.mapTo(it.key) --> menu.selectBus.writer,
      onClick.mapTo(false) --> menu.popover.openVar.writer
    )
    root
  }
}
