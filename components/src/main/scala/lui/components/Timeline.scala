package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Timeline private[components] (val root: HtmlElement) extends Component {
  private[components] val itemsVar: Var[Seq[Timeline.Item]] = Var(Seq.empty)
}

/** Vertical sequence of events. */
object Timeline extends ComponentFactory[Timeline] {

  final case class Item(title: String, meta: String = "", body: String = "")

  val items = Prop.in[Seq[Item], Timeline](_.itemsVar)

  override protected def build: Timeline = {
    val root = div()
    val el = new Timeline(root)

    root.amend(
      stack.col(spacing.lg),
      children <-- el.itemsVar.signal.map(_.map(renderItem).toList)
    )
    el
  }

  private def renderItem(it: Item): HtmlElement =
    div(
      stack.row(spacing.md) ++ css.alignItems("flex-start"),
      // bullet column
      div(
        css.display(Display.Flex) ++ css.flexDirection("column") ++ css.alignItems("center"),
        themed(t =>
          css.width(Length.px(10)) ++
            css.height(Length.px(10)) ++
            css.borderRadius(radius.pill) ++
            css.background(t.brand) ++
            css.raw("margin-top", "6px") ++
            css.raw("flex", "0 0 auto")
        )
      ),
      // body
      div(
        stack.col(spacing.xs),
        css.raw("flex", "1 1 auto"),
        span(typo.label, it.title),
        if (it.meta.nonEmpty) span(typo.hint, it.meta) else emptyNode,
        if (it.body.nonEmpty) span(typo.muted, it.body) else emptyNode
      )
    )
}
