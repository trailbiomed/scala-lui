package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Menu private[components] (
    val root: HtmlElement,
    private[components] val popover: Popover
) extends Component {
  private[components] val itemsVar: Var[Seq[Menu.Item]] = Var(Seq.empty)
  private[components] val selectBus: EventBus[String] = new EventBus[String]
}

/** Click-toggled action menu, built on `Popover`. */
object Menu extends ComponentFactory[Menu] {

  final case class Item(key: String, label: String, icon: String = "", danger: Boolean = false)

  val items = Prop.in[Seq[Item], Menu](_.itemsVar)
  val select = Prop.out[String, Menu](_.selectBus)

  def trigger(content: Modifier[HtmlElement]*): Mod[Menu] = el =>
    Popover.trigger(content*)(el.popover)

  override protected def build: Menu = {
    val popover = Popover()
    val el = new Menu(popover.root, popover)

    Popover.body(
      div(
        stack.col(Length.zero),
        children <-- el.itemsVar.signal.map { its =>
          its.map(i => itemEl(i, el)).toList
        }
      )
    )(popover)

    el
  }

  private def itemEl(it: Item, menu: Menu): HtmlElement = {
    val root = div()
    val interact = Interactive.on(root)
    root.amend(
      interact.state.styled { (t, i) =>
        val fg = if (it.danger) t.danger else t.text
        val bg = if (i.hovered) t.surfaceDim else Color.transparent
        stack.row(spacing.sm) ++
          css.padding(Length.px(6), spacing.md) ++
          css.cursor("pointer") ++
          css.color(fg) ++
          css.background(bg) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.lg)
      },
      if (it.icon.nonEmpty) span(it.icon) else emptyNode,
      span(it.label),
      onClick.mapToUnit --> Observer[Unit] { _ =>
        menu.selectBus.writer.onNext(it.key)
        menu.popover.openVar.set(false)
      }
    )
    root
  }
}
