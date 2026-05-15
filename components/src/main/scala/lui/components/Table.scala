package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Table private[components] (val root: HtmlElement) extends Component {
  private[components] val columnsVar: Var[Seq[String]] = Var(Seq.empty)
  private[components] val rowsVar: Var[Seq[Seq[String]]] = Var(Seq.empty)
  private[components] val stripedVar: Var[Boolean] = Var(false)
}

/** Minimal themed table. Pass column headers and rows of strings. For richer cell content
  * (links, badges, etc.) use the slot-based `Table.body` API. */
object Table extends ComponentFactory[Table] {

  val columns = Prop.in[Seq[String], Table](_.columnsVar)
  val rows = Prop.in[Seq[Seq[String]], Table](_.rowsVar)
  val striped = Prop.in[Boolean, Table](_.stripedVar)

  override protected def build: Table = {
    val root = table()
    val el = new Table(root)

    root.amend(
      themed(_ =>
        css.width(Length.pct(100)) ++
          css.raw("border-collapse", "collapse") ++
          css.fontSize(fontSizes.lg)
      ),
      thead(
        tr(
          children <-- el.columnsVar.signal.map { cs =>
            cs.map { c =>
              th(
                themed(t =>
                  css.padding(spacing.md, spacing.lg) ++
                    css.textAlign(TextAlign.Left) ++
                    css.color(t.textMuted) ++
                    css.fontWeight(FontWeight.Medium) ++
                    css.raw("border-bottom", s"1px solid ${t.border.toCss}")
                ),
                c
              )
            }.toList
          }
        )
      ),
      tbody(
        children <-- Signal.combine(el.rowsVar.signal, el.stripedVar.signal).map {
          case (rs, striped) =>
            rs.zipWithIndex.map { case (cells, i) =>
              tr(
                themed(t =>
                  if (striped && i % 2 == 1) css.background(t.surfaceDim) else Style.empty
                ),
                cells.map { v =>
                  td(
                    themed(t =>
                      css.padding(spacing.md, spacing.lg) ++
                        css.color(t.text) ++
                        css.raw("border-bottom", s"1px solid ${t.border.toCss}")
                    ),
                    v
                  )
                }.toList
              )
            }.toList
        }
      )
    )

    el
  }
}
