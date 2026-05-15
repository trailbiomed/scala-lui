package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class SegmentedControl private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val optionsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val disabledVar: Var[Boolean] = Var(false)
}

/** A row of mutually-exclusive buttons. Pick one of N. Use this instead of `RadioGroup` when
  * the choices are short, primary-ish actions you want presented as a single control (e.g.
  * "All / Active / Archived" filter, "Day / Week / Month" toggle). */
object SegmentedControl extends ComponentFactory[SegmentedControl] {

  val value = Prop.inOut[String, SegmentedControl](_.valueVar)
  val options = Prop.in[Seq[(String, String)], SegmentedControl](_.optionsVar)
  val disabled = Prop.in[Boolean, SegmentedControl](_.disabledVar)

  override protected def build: SegmentedControl = {
    val root = div()
    val el = new SegmentedControl(root)

    root.amend(
      themed(t =>
        css.display(Display.InlineFlex) ++
          css.padding(Length.px(3)) ++
          css.background(t.surfaceDim) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.borderRadius(radius.md)
      ),
      children <-- Signal
        .combine(el.optionsVar.signal, el.valueVar.signal, el.disabledVar.signal)
        .map { case (opts, current, d) =>
          opts.map { case (key, lbl) =>
            segmentEl(key, lbl, current == key, d, el.valueVar)
          }.toList
        }
    )
    el
  }

  private def segmentEl(
      key: String,
      lbl: String,
      selected: Boolean,
      disabled: Boolean,
      valueVar: Var[String]
  ): HtmlElement = {
    val hovered = Var(false)
    button(
      typ := "button",
      hovered.signal.styled { (t, hov) =>
        val (bg, fg) = (selected, disabled, hov) match {
          case (_, true, _)        => (Color.transparent, t.textSubtle)
          case (true, _, _)        => (t.surface, t.text)
          case (false, _, true)    => (Color.transparent, t.text)
          case (false, _, false)   => (Color.transparent, t.textMuted)
        }
        val shadow = if (selected) "0 1px 3px rgba(0,0,0,0.10)" else "none"
        css.padding(Length.px(4), spacing.lg) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(if (selected) FontWeight.SemiBold else FontWeight.Medium) ++
          css.color(fg) ++
          css.background(bg) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.borderRadius(radius.sm) ++
          css.cursor(if (disabled) "not-allowed" else "pointer") ++
          css.raw("font-family", "inherit") ++
          css.raw("box-shadow", shadow) ++
          css.transition("background", 120) ++
          css.raw("white-space", "nowrap")
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onClick.mapToUnit.filter(_ => !disabled) -->
        Observer[Unit](_ => valueVar.set(key)),
      lbl
    )
  }
}
