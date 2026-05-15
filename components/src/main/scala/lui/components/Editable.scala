package lui.components

import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Editable private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val placeholderVar: Var[String] = Var("Click to edit")
  private[components] val editingVar: Var[Boolean] = Var(false)
}

/** Click-to-edit text. Displays plain text; clicking enters input mode. Enter commits,
  * Escape cancels. Blur commits. */
object Editable extends ComponentFactory[Editable] {

  val value = Prop.inOut[String, Editable](_.valueVar)
  val placeholder = Prop.in[String, Editable](_.placeholderVar)
  val editing = Prop.inOut[Boolean, Editable](_.editingVar)

  override protected def build: Editable = {
    val draft = Var("")
    val inputEl = input()
    val previewEl = span()
    val root = div()
    val el = new Editable(root)

    def startEdit(): Unit = {
      draft.set(el.valueVar.now())
      el.editingVar.set(true)
      // Focus on next tick (after render)
      val _ = scala.scalajs.js.timers.setTimeout(0)(inputEl.ref.focus())
    }
    def commit(): Unit = {
      el.valueVar.set(draft.now())
      el.editingVar.set(false)
    }
    def cancel(): Unit = {
      el.editingVar.set(false)
    }

    root.amend(
      child <-- el.editingVar.signal.map(if (_) inputEl else previewEl)
    )

    previewEl.amend(
      Signal.combine(el.valueVar.signal, el.placeholderVar.signal, el.interact.state).styled {
        case (t, (v, _, i)) =>
          val empty = v.isEmpty
          typo.body.resolve(t) ++
            css.padding(Length.px(4), Length.px(6)) ++
            css.borderRadius(radius.sm) ++
            css.cursor("text") ++
            css.color(if (empty) t.textSubtle else t.text) ++
            (if (i.hovered) css.background(t.surfaceDim) else Style.empty)
      },
      child.text <-- Signal.combine(el.valueVar.signal, el.placeholderVar.signal).map {
        case (v, ph) => if (v.isEmpty) ph else v
      },
      onClick.mapToUnit --> Observer[Unit](_ => startEdit())
    )

    inputEl.amend(
      typ := "text",
      themed(t =>
        css.padding(Length.px(4), Length.px(6)) ++
          css.fontSize(Length.px(15)) ++
          css.color(t.text) ++
          css.background(t.surface) ++
          css.border(Length.px(1.5), BorderStyle.Solid, t.borderActive) ++
          css.borderRadius(radius.sm) ++
          css.raw("outline", "none") ++
          css.raw("font-family", "inherit")
      ),
      htmlValue <-- draft.signal,
      onInput.mapToValue --> draft.writer,
      onBlur.mapToUnit --> Observer[Unit](_ => commit()),
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        ev.key match {
          case "Enter"  => ev.preventDefault(); commit()
          case "Escape" => ev.preventDefault(); cancel()
          case _        => ()
        }
      }
    )

    el
  }
}
