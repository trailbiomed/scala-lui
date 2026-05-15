package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Alert private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val titleVar: Var[String] = Var("")
  private[components] val variantVar: Var[Alert.Variant] = Var(Alert.Variant.Info)
  private[components] val dismissibleVar: Var[Boolean] = Var(false)
  private[components] val dismissBus: EventBus[Unit] = new EventBus[Unit]
  private[components] val closeHover: Var[Boolean] = Var(false)
}

/** Inline notification with a severity color and optional dismiss button. Unlike `Toast`,
  * `Alert` is in the document flow — use it for persistent messages near the content they
  * describe. */
object Alert extends ComponentFactory[Alert] {

  enum Variant { case Info, Success, Warning, Danger }

  val title = Prop.in[String, Alert](_.titleVar)
  val variant = Prop.in[Variant, Alert](_.variantVar)
  val dismissible = Prop.in[Boolean, Alert](_.dismissibleVar)
  val dismiss = Prop.out[Unit, Alert](_.dismissBus)

  /** Slot for the alert body (paragraphs, links, etc.). */
  def body(content: Modifier[HtmlElement]*): Mod[Alert] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Alert = {
    val iconEl = span()
    val titleEl = span()
    val bodySlot = div()
    val closeEl = span()

    val textCol = div(
      stack.col(spacing.xs) ++ stack.grow,
      titleEl,
      bodySlot
    )

    val root = div(iconEl, textCol, closeEl)
    val el = new Alert(root, bodySlot)

    root.amend(
      el.variantVar.signal.styled { (t, v) =>
        val (bg, fg, bd) = colorsFor(t, v)
        stack.row(spacing.md) ++
          css.raw("align-items", "flex-start") ++
          css.padding(spacing.lg, spacing.xl) ++
          css.borderRadius(radius.md) ++
          css.background(bg) ++
          css.color(fg) ++
          css.border(Length.px(1), BorderStyle.Solid, bd)
      }
    )

    iconEl.amend(
      el.variantVar.signal.styled { (t, v) =>
        css.color(colorsFor(t, v)._2) ++
          css.fontSize(fontSizes.xxl) ++
          css.fontWeight(FontWeight.Bold) ++
          css.raw("line-height", "1") ++
          stack.noShrink ++
          css.raw("margin-top", "1px")
      },
      child.text <-- el.variantVar.signal.map(iconFor)
    )

    titleEl.amend(
      el.titleVar.signal.styled { (_, lbl) =>
        css.fontSize(fontSizes.xl) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.display(if (lbl.nonEmpty) Display.Block else Display.None)
      },
      child.text <-- el.titleVar.signal
    )

    bodySlot.amend(
      typo.body ++ css.lineHeight(1.5)
    )

    closeEl.amend(
      Signal.combine(el.dismissibleVar.signal, el.closeHover.signal).styled {
        case (t, (dismissible, hov)) =>
          css.display(if (dismissible) Display.InlineFlex else Display.None) ++
            css.alignItems("center") ++ css.justifyContent("center") ++
            css.cursor("pointer") ++
            css.opacity(if (hov) 1.0 else 0.65) ++
            css.fontSize(fontSizes.xxl) ++
            css.raw("line-height", "1") ++
            css.color(t.text) ++
            css.padding(Length.px(0), Length.px(2)) ++
            css.transition("opacity", 120) ++
            stack.noShrink ++
            css.raw("user-select", "none")
      },
      "×",
      onMouseEnter.mapTo(true) --> el.closeHover.writer,
      onMouseLeave.mapTo(false) --> el.closeHover.writer,
      onClick.stopPropagation.mapToUnit --> el.dismissBus.writer
    )

    el
  }

  private def colorsFor(t: Theme, v: Variant): (Color, Color, Color) = v match {
    case Variant.Info    => (t.infoSoft, t.info, t.infoBorder)
    case Variant.Success => (t.successSoft, t.success, t.successBorder)
    case Variant.Warning => (t.warningSoft, t.warning, t.warningBorder)
    case Variant.Danger  => (t.dangerSoft, t.danger, t.dangerBorder)
  }

  private def iconFor(v: Variant): String = v match {
    case Variant.Info    => "ⓘ"
    case Variant.Success => "✓"
    case Variant.Warning => "!"
    case Variant.Danger  => "!"
  }
}
