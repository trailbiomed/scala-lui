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

  /** @param disabled renders the row muted and inert: it emits nothing, and arrow-key
    *                  navigation skips over it. For a row that stays visible only to
    *                  explain itself, such as "No tags yet". */
  final case class Item(
      key: String,
      label: String,
      icon: String = "",
      danger: Boolean = false,
      disabled: Boolean = false
  )

  private val focusableItemSelector = "[role='menuitem']:not([aria-disabled='true'])"

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
          val items = container.querySelectorAll(focusableItemSelector)
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
          val items = popover.bodySlot.ref.querySelectorAll(focusableItemSelector)
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
      aria.disabled := it.disabled,
      tabIndex := (if (it.disabled) -1 else 0),
      interact.state.styled { (t, i) =>
        val fg =
          if (it.disabled) t.textMuted
          else if (it.danger) t.danger
          else t.text
        val bg =
          if (it.disabled) Color.transparent
          else if (i.hovered || i.focused) t.surfaceDim
          else Color.transparent
        val ring =
          if (i.focusVisible && !i.pressed && !it.disabled)
            css.raw("box-shadow", s"0 0 0 2px ${t.brand.alpha(0.35).toCss}")
          else css.raw("box-shadow", "none")
        stack.row(spacing.sm) ++
          css.padding(Length.px(6), spacing.md) ++
          css.cursor(if (it.disabled) "default" else "pointer") ++
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
      onClick.filter(_ => !it.disabled).mapTo(it.key) --> menu.selectBus.writer,
      onClick.filter(_ => !it.disabled).mapTo(false) --> menu.popover.openVar.writer
    )
    root
  }
}
