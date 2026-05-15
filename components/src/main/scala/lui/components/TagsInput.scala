package lui.components

import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, placeholder as htmlPlaceholder, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class TagsInput private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Seq[String]] = Var(Seq.empty)
  private[components] val placeholderVar: Var[String] = Var("Add tag…")
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val focused: Var[Boolean] = Var(false)
  private[components] val draftVar: Var[String] = Var("")
}

/** Multi-value input: type and press Enter or comma to commit a tag. Backspace on an empty
  * draft removes the last tag. */
object TagsInput extends ComponentFactory[TagsInput] {

  val value = Prop.inOut[Seq[String], TagsInput](_.valueVar)
  val placeholder = Prop.in[String, TagsInput](_.placeholderVar)
  val disabled = Prop.in[Boolean, TagsInput](_.disabledVar)

  override protected def build: TagsInput = {
    val draftInput = input()
    val root = div()
    val el = new TagsInput(root)

    def commitDraft(): Unit = {
      val raw = el.draftVar.now().trim
      if (raw.nonEmpty && !el.valueVar.now().contains(raw)) {
        el.valueVar.update(_ :+ raw)
      }
      el.draftVar.set("")
    }

    root.amend(
      Signal.combine(el.focused.signal, el.disabledVar.signal).styled {
        case (t, (focusedOn, d)) =>
          val bd = if (focusedOn) t.borderActive else t.border
          val shadow =
            if (focusedOn) s"0 0 0 3px ${t.brand.alpha(0.18).toCss}" else "none"
          css.display(Display.Flex) ++
            css.flexWrap("wrap") ++
            css.gap(spacing.xs) ++
            css.alignItems("center") ++
            css.padding(Length.px(6), Length.px(8)) ++
            css.background(if (d) t.surfaceDim else t.surface) ++
            css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
            css.borderRadius(radius.md) ++
            css.raw("box-shadow", shadow) ++
            css.transition("border-color", 150)
      },
      children <-- el.valueVar.signal.map { tags =>
        tags.toList.map(tagChip(_, el.valueVar)) :+ draftInput
      },
      onClick.mapToUnit --> Observer[Unit](_ => draftInput.ref.focus())
    )

    draftInput.amend(
      themed(t =>
        css.raw("flex", "1 1 80px") ++
          css.raw("min-width", "80px") ++
          css.padding(Length.px(4), Length.px(4)) ++
          css.fontSize(Length.px(15)) ++
          css.color(t.text) ++
          css.background(Color.transparent) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none")
      ),
      htmlValue <-- el.draftVar.signal,
      htmlPlaceholder <-- el.placeholderVar.signal,
      onInput.mapToValue --> el.draftVar.writer,
      onFocus.mapTo(true) --> el.focused.writer,
      onBlur.mapTo(false) --> el.focused.writer,
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        ev.key match {
          case "Enter" | "," =>
            ev.preventDefault()
            commitDraft()
          case "Backspace" if el.draftVar.now().isEmpty =>
            el.valueVar.update(v => v.dropRight(1))
          case _ => ()
        }
      },
      onBlur.mapToUnit --> Observer[Unit](_ => commitDraft())
    )

    el
  }

  private def tagChip(value: String, store: Var[Seq[String]]): HtmlElement =
    span(
      themed(t =>
        stack.row(spacing.xs) ++
          css.padding(Length.px(2), spacing.sm) ++
          css.background(t.brandSoft) ++
          css.color(t.brand) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.lg)
      ),
      span(value),
      span(
        themed(t => css.cursor("pointer") ++ css.color(t.textMuted)),
        "×",
        onClick.stopPropagation.mapToUnit -->
          Observer[Unit](_ => store.update(_.filterNot(_ == value)))
      )
    )
}
