package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class KbdList private[components] (val root: HtmlElement) extends Component {
  private[components] val entriesVar: Var[Seq[KbdList.Entry]] = Var(Seq.empty)
  private[components] val gapVar: Var[Length] = Var(spacing.sm)
}

/** A vertical list of keyboard-shortcut rows. Each row shows one or more
  * `Kbd` chips on the left and a description string on the right — the
  * canonical layout of a "keyboard shortcuts" help panel.
  *
  * Multi-key entries render as separate `Kbd` chips (for chords like
  * `⌘` + `K`, or synonyms like `→` / `PageDown` for the same action).
  *
  * {{{
  *   KbdList(
  *     KbdList.entries := Seq(
  *       KbdList.Entry(Seq("⌘", "K"),        "Open the command palette"),
  *       KbdList.Entry(Seq("→", "PageDown"), "Next page"),
  *       KbdList.Entry(Seq("Esc"),           "Close")
  *     )
  *   )
  * }}}
  */
object KbdList extends ComponentFactory[KbdList] {

  /** A single row: one or more key labels + a description. */
  final case class Entry(keys: Seq[String], description: String)

  val entries = Prop.in[Seq[Entry], KbdList](_.entriesVar)

  /** Vertical gap between rows. Default `spacing.sm`. */
  val gap = Prop.in[Length, KbdList](_.gapVar)

  override protected def build: KbdList = {
    val root = div()
    val el = new KbdList(root)

    root.amend(
      el.gapVar.signal.styled { (_, g) => stack.col(g) },
      children <-- el.entriesVar.signal.map(_.map(row).toList)
    )
    el
  }

  private def row(entry: Entry): HtmlElement =
    div(
      stack.between(spacing.md) ++ css.alignItems("center"),
      div(
        stack.row(spacing.xs) ++ css.alignItems("center"),
        entry.keys.map(k => Kbd(Kbd.key := k).root).toList
      ),
      span(
        themed(t => css.color(t.textMuted) ++ css.fontSize(fontSizes.md)),
        entry.description
      )
    )
}
