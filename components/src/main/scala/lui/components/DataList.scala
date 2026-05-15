package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class DataList private[components] (val root: HtmlElement) extends Component {
  private[components] val itemsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val orientationVar: Var[DataList.Orientation] =
    Var(DataList.Orientation.Horizontal)
}

/** Key/value pairs panel. */
object DataList extends ComponentFactory[DataList] {

  enum Orientation { case Horizontal, Vertical }

  val items = Prop.in[Seq[(String, String)], DataList](_.itemsVar)
  val orientation = Prop.in[Orientation, DataList](_.orientationVar)

  override protected def build: DataList = {
    val root = div()
    val el = new DataList(root)

    root.amend(
      stack.col(spacing.sm),
      children <-- Signal.combine(el.itemsVar.signal, el.orientationVar.signal).map {
        case (its, o) =>
          its.map { case (k, v) => row(k, v, o) }.toList
      }
    )
    el
  }

  private def row(k: String, v: String, o: Orientation): HtmlElement =
    o match {
      case Orientation.Horizontal =>
        div(
          stack.between(spacing.lg),
          span(typo.muted, k),
          span(typo.body, v)
        )
      case Orientation.Vertical =>
        div(
          stack.col(spacing.xs),
          span(typo.eyebrow, k),
          span(typo.body, v)
        )
    }
}
