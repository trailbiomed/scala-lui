package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Breadcrumb private[components] (val root: HtmlElement) extends Component {
  private[components] val itemsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val selectBus: EventBus[String] = new EventBus[String]
}

/** Hierarchical navigation trail. `items` is `Seq[(key, label)]`; the last item is rendered
  * as a non-clickable "current" crumb. Clicking any other crumb emits its key on `select`. */
object Breadcrumb extends ComponentFactory[Breadcrumb] {

  val items = Prop.in[Seq[(String, String)], Breadcrumb](_.itemsVar)

  val select = Prop.out[String, Breadcrumb](_.selectBus)

  override protected def build: Breadcrumb = {
    val root = navTag()
    val el = new Breadcrumb(root)

    root.amend(
      themed(_ => stack.row(spacing.xs) ++ stack.wrap),
      children <-- el.itemsVar.signal.map { its =>
        val n = its.size
        its.zipWithIndex.flatMap { case ((key, lbl), idx) =>
          val isLast = idx == n - 1
          val crumb = crumbEl(key, lbl, isLast, el.selectBus.writer)
          if (isLast) Seq(crumb)
          else Seq(crumb, separator)
        }.toList
      }
    )
    el
  }

  private def crumbEl(
      key: String,
      lbl: String,
      isLast: Boolean,
      sink: Observer[String]
  ): HtmlElement = {
    val hovered = Var(false)
    val node =
      if (isLast) span()
      else button(typ := "button")
    node.amend(
      hovered.signal.styled { (t, hov) =>
        val baseColor =
          if (isLast) t.text
          else if (hov) t.brand
          else t.textMuted
        css.fontSize(fontSizes.lg) ++
          css.fontWeight(if (isLast) FontWeight.SemiBold else FontWeight.Medium) ++
          css.color(baseColor) ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.padding(Length.px(0)) ++
          css.cursor(if (isLast) "default" else "pointer") ++
          css.raw("font-family", "inherit") ++
          css.transition("color", 120)
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      lbl
    )
    if (!isLast) {
      node.amend(
        onClick.preventDefault.mapTo(key) --> sink
      )
    }
    node
  }

  private def separator: HtmlElement =
    span(
      themed(t => css.color(t.textSubtle) ++ css.fontSize(fontSizes.lg)),
      "›"
    )
}
