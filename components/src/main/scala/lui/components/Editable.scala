package lui.components

import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Editable private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val placeholderVar: Var[String] = Var("Click to edit")
  private[components] val editingVar: Var[Boolean] = Var(false)
  private[components] val variantVar: Var[Editable.Variant] = Var(Editable.Variant.Body)
}

/** Click-to-edit text. Displays plain text; clicking enters input mode. Enter commits,
  * Escape cancels. Blur commits.
  *
  * Two visual variants:
  *   - `Body` (default): 14-px regular text. Preview has a small
  *     padded background-on-hover affordance; input is a bordered
  *     box with the same padding.
  *   - `Heading`: 16-px semibold heading-style text. Preview has no
  *     hover background — just a brand-color hover. Input has only
  *     a 2px bottom border (matches the "click to rename a title"
  *     pattern in nextweb-style reference detail pages). */
object Editable extends ComponentFactory[Editable] {

  enum Variant { case Body, Heading }

  val value = Prop.inOut[String, Editable](_.valueVar)
  val placeholder = Prop.in[String, Editable](_.placeholderVar)
  val editing = Prop.inOut[Boolean, Editable](_.editingVar)
  val variant = Prop.in[Variant, Editable](_.variantVar)

  override protected def build: Editable = {
    val draft = Var("")
    val inputEl = input()
    val previewEl = span()
    val root = div()
    val el = new Editable(root)

    def commit(): Unit = {
      el.valueVar.set(draft.now())
      el.editingVar.set(false)
    }
    def cancel(): Unit = {
      el.editingVar.set(false)
    }

    // Becomes-editing trigger fires for both click-on-preview AND
    // external `editing := true` writes from the parent (e.g. a
    // "Rename" button). On every entry into edit mode, seed the
    // draft from the current value and focus the input.
    val becomesEditing: EventStream[Unit] =
      el.editingVar.signal.changes.collect { case true => () }

    root.amend(
      child <-- el.editingVar.signal.map(if (_) inputEl else previewEl),
      becomesEditing
        .compose(_.withCurrentValueOf(el.valueVar.signal))
        --> draft.writer,
      becomesEditing --> Observer[Unit] { _ =>
        val _ = scala.scalajs.js.timers.setTimeout(0)(inputEl.ref.focus())
      },
    )

    previewEl.amend(
      Signal
        .combine(el.valueVar.signal, el.placeholderVar.signal, el.variantVar.signal, el.interact.state)
        .styled { case (t, (v, _, vrt, i)) =>
          val empty = v.isEmpty
          vrt match {
            case Variant.Body =>
              typo.body.resolve(t) ++
                css.padding(Length.px(4), Length.px(6)) ++
                css.borderRadius(radius.sm) ++
                css.cursor("text") ++
                css.color(if (empty) t.textSubtle else t.text) ++
                (if (i.hovered) css.background(t.surfaceDim) else Style.empty)
            case Variant.Heading =>
              css.fontSize(fontSizes.xxl) ++
                css.fontWeight(FontWeight.SemiBold) ++
                css.cursor("pointer") ++
                css.padding(Length.zero) ++
                css.margin(Length.zero) ++
                css.color {
                  if (empty) t.textSubtle
                  else if (i.hovered) t.brand
                  else t.text
                } ++
                css.transition("color", 150)
          }
        },
      child.text <-- Signal.combine(el.valueVar.signal, el.placeholderVar.signal).map {
        case (v, ph) => if (v.isEmpty) ph else v
      },
      // Click flips editing on; the `becomesEditing` stream above
      // handles draft-seeding and focus uniformly for both click and
      // external `editing := true` writes.
      onClick.mapTo(true) --> el.editingVar.writer
    )

    inputEl.amend(
      typ := "text",
      el.variantVar.signal.styled { (t, vrt) =>
        vrt match {
          case Variant.Body =>
            css.padding(Length.px(4), Length.px(6)) ++
              css.fontSize(Length.px(15)) ++
              css.color(t.text) ++
              css.background(t.surface) ++
              css.border(Length.px(1), BorderStyle.Solid, t.borderActive) ++
              css.borderRadius(radius.sm) ++
              css.raw("outline", "none") ++
              css.raw("font-family", "inherit")
          case Variant.Heading =>
            css.fontSize(fontSizes.xxl) ++
              css.fontWeight(FontWeight.SemiBold) ++
              css.color(t.text) ++
              css.background(Color.transparent) ++
              css.padding(Length.zero) ++
              css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
              css.borderBottom(Length.px(2), BorderStyle.Solid, t.brand) ++
              css.borderRadius(Length.zero) ++
              css.raw("outline", "none") ++
              css.raw("font-family", "inherit") ++
              css.width(Length.pct(100))
        }
      },
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
