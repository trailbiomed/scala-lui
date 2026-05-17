package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ListEditor private[components] (val root: HtmlElement) extends Component {
  import ListEditor.Row
  private[components] val rowsVar: Var[Vector[Row]] = Var(Vector(Row(1L, "")))
  private[components] val placeholderVar: Var[String] = Var("")
  private[components] val addLabelVar: Var[String] = Var("+ Add")
  private[components] val minRowsVar: Var[Int] = Var(1)
  private[components] val dropBlankVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.pct(100))
  private[components] var nextIdCounter: Long = 1L

  private[components] def newId(): Long = {
    nextIdCounter += 1
    nextIdCounter
  }
}

/** Repeating-row editor for a `Var[Seq[String]]`. Each row is a `TextInput`
  * with a delete button; an "+ Add" button appends a fresh row at the bottom.
  *
  * Each row has a stable internal id so adding/removing a row preserves focus
  * in the other rows. Outbound emissions are deduped by Seq equality. */
object ListEditor extends ComponentFactory[ListEditor] {

  private[components] final case class Row(id: Long, value: String)

  val placeholder = Prop.in[String, ListEditor](_.placeholderVar)
  val addLabel = Prop.in[String, ListEditor](_.addLabelVar)
  val minRows = Prop.in[Int, ListEditor](_.minRowsVar)
  val width = Prop.in[Length, ListEditor](_.widthVar)

  /** When true, blank rows are filtered out of outbound emissions.
    * Default false. */
  val dropBlank = Prop.in[Boolean, ListEditor](_.dropBlankVar)

  /** Two-way bind to a `Var[Seq[String]]`. The `<--` direction replaces all
    * rows with fresh ids (loses focus). The `-->` direction emits whenever
    * any row changes (deduped by Seq equality). `<-->` does both. */
  val value: InOut[Seq[String], ListEditor] = new InOut[Seq[String], ListEditor](
    bindIn = (el, src) => {
      val _ = el.root.amend(
        src.toObservable --> Observer[Seq[String]] { ss =>
          val current = el.rowsVar.now().map(_.value).toSeq
          if (current != ss) {
            val rows =
              if (ss.isEmpty) Vector(Row(el.newId(), ""))
              else ss.iterator.map(v => Row(el.newId(), v)).toVector
            el.rowsVar.set(rows)
          }
        }
      )
    },
    bindOut = (el, sink) => {
      val _ = el.root.amend(
        Signal
          .combine(el.rowsVar.signal, el.dropBlankVar.signal)
          .map { case (rows, drop) =>
            val raw = rows.map(_.value).toSeq
            if (drop) raw.filter(_.nonEmpty) else raw
          }
          .changes
          .distinct --> sink
      )
    }
  )

  override protected def build: ListEditor = {
    val root = div()
    val el = new ListEditor(root)

    def rowUI(id: Long, initial: Row, rowSig: Signal[Row]): HtmlElement = {
      val rowVar = Var(initial.value)
      val _ = rowSig
      val disabledSig = Signal.combine(el.rowsVar.signal, el.minRowsVar.signal)
        .map { case (rows, min) => rows.length <= min }
        .distinct
      div(
        stack.row(spacing.sm) ++ css.alignItems("center"),
        rowVar.signal.changes --> Observer[String] { v =>
          el.rowsVar.update(_.map(r => if (r.id == id) r.copy(value = v) else r))
        },
        div(
          stack.grow ++ css.raw("min-width", "0"),
          TextInput(
            TextInput.value <--> rowVar,
            TextInput.placeholder <-- el.placeholderVar.signal,
            TextInput.width := Length.pct(100)
          )
        ),
        IconButton(
          IconButton.icon := "×",
          IconButton.ariaLabel := "Remove row",
          IconButton.variant := IconButton.Variant.Ghost,
          IconButton.size := IconButton.Size.Small,
          IconButton.disabled <-- disabledSig,
          IconButton.click --> Observer[Unit] { _ =>
            val minR = el.minRowsVar.now()
            el.rowsVar.update { rows =>
              if (rows.length <= minR) rows
              else rows.filterNot(_.id == id)
            }
          }
        )
      )
    }

    root.amend(
      el.widthVar.signal.styled { (_, w) =>
        stack.col(spacing.sm) ++ css.width(w)
      },
      // Split keys on row id — so each row's child element is preserved across
      // add/remove, and focus inside the surviving rows is not lost.
      children <-- el.rowsVar.signal
        .split(_.id)((id, init, sig) => rowUI(id, init, sig)),
      // Add row button at the bottom.
      div(
        Button(
          Button.label <-- el.addLabelVar.signal,
          Button.variant := Button.Variant.Ghost,
          Button.size := Button.Size.Small,
          Button.click --> Observer[Unit] { _ =>
            el.rowsVar.update(_ :+ Row(el.newId(), ""))
          }
        )
      )
    )

    el
  }
}
