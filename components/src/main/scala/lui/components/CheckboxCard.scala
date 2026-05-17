package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class CheckboxCard private[components] (val root: HtmlElement) extends Component {
  private[components] val titleVar: Var[String] = Var("")
  private[components] val descriptionVar: Var[String] = Var("")
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
}

/** A checkbox presented as a clickable card with a title and description. */
object CheckboxCard extends ComponentFactory[CheckboxCard] {

  val title = Prop.in[String, CheckboxCard](_.titleVar)
  val description = Prop.in[String, CheckboxCard](_.descriptionVar)
  val checked = Prop.inOut[Boolean, CheckboxCard](_.checkedVar)
  val disabled = Prop.in[Boolean, CheckboxCard](_.disabledVar)

  private val boxSize: Length = Length.px(16)

  override protected def build: CheckboxCard = {
    val box = span()
    val root = button(typ := "button")
    val el = new CheckboxCard(root)

    root.amend(
      role := "checkbox",
      aria.checked <-- el.checkedVar.signal.map(_.toString),
      aria.disabled <-- el.disabledVar.signal,
      Signal.combine(el.checkedVar.signal, el.disabledVar.signal, el.interact.state).styled {
        case (t, (on, d, i)) =>
          val bd =
            if (d) t.border
            else if (on) t.brand
            else if (i.hovered) t.borderActive
            else t.border
          val ring =
            if (i.focused && !i.pressed && !d)
              css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.3).toCss}")
            else css.raw("box-shadow", "none")
          stack.row(spacing.md) ++
            css.alignItems("flex-start") ++
            css.padding(spacing.lg) ++
            css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
            css.borderRadius(radius.md) ++
            css.background(if (on) t.brandSoft else t.surface) ++
            css.color(t.text) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.opacity(if (d) 0.55 else 1.0) ++
            css.raw("font-family", "inherit") ++
            css.raw("text-align", "left") ++
            css.raw("outline", "none") ++
            css.transition("border-color", 150) ++
            ring
      },
      onClick.preventDefault.mapToUnit.filter(_ => !el.disabledVar.now()) -->
        Observer[Unit](_ => el.checkedVar.update(c => !c)),
      box,
      div(
        stack.col(spacing.xs),
        span(typo.label, child.text <-- el.titleVar.signal),
        span(typo.muted, child.text <-- el.descriptionVar.signal)
      )
    )

    box.amend(
      el.checkedVar.signal.styled { (t, on) =>
        val (bg, bd) = if (on) (t.brand, t.brand) else (t.surface, t.border)
        stack.centerAll ++
          css.width(boxSize) ++ css.height(boxSize) ++
          css.borderRadius(Length.px(4)) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.background(bg) ++
          css.color(t.onBrand) ++
          stack.noShrink ++
          css.raw("margin-top", "2px")
      },
      child.maybe <-- el.checkedVar.signal.map { on =>
        if (on) Some(Checkmark(Length.px(11))) else None
      }
    )

    el
  }
}
