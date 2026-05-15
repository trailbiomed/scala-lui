package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class CloseButton private[components] (val root: HtmlElement) extends Component {
  private[components] val sizeVar: Var[CloseButton.Size] = Var(CloseButton.Size.Medium)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val ariaLabelVar: Var[String] = Var("Close")
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]
}

/** A round-corner `×` button. Use inside Modals, Toasts, Tags, etc., wherever a "dismiss"
  * affordance is needed. Default `ariaLabel` is `"Close"` — override when context-specific. */
object CloseButton extends ComponentFactory[CloseButton] {

  enum Size { case Small, Medium }

  val size = Prop.in[Size, CloseButton](_.sizeVar)
  val disabled = Prop.in[Boolean, CloseButton](_.disabledVar)
  val ariaLabel = Prop.in[String, CloseButton](_.ariaLabelVar)
  val click = Prop.out[Unit, CloseButton](_.clickBus)

  override protected def build: CloseButton = {
    val root = button(typ := "button")
    val el = new CloseButton(root)

    root.amend(
      Signal.combine(el.sizeVar.signal, el.disabledVar.signal, el.interact.state).styled {
        case (t, (sz, d, i)) =>
          val dim = sz match {
            case Size.Small  => Length.px(22)
            case Size.Medium => Length.px(28)
          }
          val fsz = sz match {
            case Size.Small  => fontSizes.lg
            case Size.Medium => fontSizes.xl
          }
          val (bg, fg) =
            if (d) (Color.transparent, t.textSubtle)
            else if (i.hovered) (t.surfaceDim, t.text)
            else (Color.transparent, t.textMuted)
          stack.centerAll ++
            css.width(dim) ++ css.height(dim) ++
            css.borderRadius(radius.md) ++
            css.fontSize(fsz) ++
            css.color(fg) ++
            css.background(bg) ++
            css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.raw("font-family", "inherit") ++
            css.raw("line-height", "1") ++
            css.transition("background", 120)
      },
      aria.label <-- el.ariaLabelVar.signal,
      aria.disabled <-- el.disabledVar.signal,
      "×",
      onClick.preventDefault.mapToUnit
        .filter(_ => !el.disabledVar.now()) --> el.clickBus.writer
    )
    el
  }
}
