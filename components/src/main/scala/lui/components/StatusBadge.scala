package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import scala.scalajs.js
import scala.scalajs.js.timers.SetIntervalHandle

final class StatusBadge private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val variantVar: Var[StatusBadge.Variant] = Var(StatusBadge.Variant.Neutral)
  private[components] val pulsingVar: Var[Boolean] = Var(false)
}

object StatusBadge extends ComponentFactory[StatusBadge] {

  enum Variant { case Success, Running, Queued, Warning, Info, Brand, Neutral }

  val label = Prop.in[String, StatusBadge](_.labelVar)
  val variant = Prop.in[Variant, StatusBadge](_.variantVar)
  val pulsing = Prop.in[Boolean, StatusBadge](_.pulsingVar)

  override protected def build: StatusBadge = {
    val root = span()
    val el = new StatusBadge(root)

    val pulseOpacity: Var[Double] = Var(1.0)
    var handle: js.UndefOr[SetIntervalHandle] = js.undefined

    root.amend(
      Signal.combine(el.variantVar.signal, el.pulsingVar.signal, pulseOpacity.signal).styled {
        case (t, (v, pulsingOn, op)) =>
          val (bg, fg, bd) = colorsFor(t, v)
          stack.row(spacing.xs) ++
            css.padding(Length.px(2), spacing.md) ++
            css.borderRadius(radius.pill) ++
            css.fontSize(fontSizes.sm) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.background(bg) ++ css.color(fg) ++
            css.border(Length.px(1), BorderStyle.Solid, bd) ++
            css.opacity(if (pulsingOn) op else 1.0) ++
            css.transition("opacity", 700)
      },
      child.text <-- el.labelVar.signal,
      onMountUnmountCallback(
        mount = _ => {
          handle = js.timers.setInterval(700.0) {
            if (el.pulsingVar.now()) {
              pulseOpacity.update(o => if (o > 0.7) 0.45 else 1.0)
            }
          }
        },
        unmount = _ => {
          handle.foreach(js.timers.clearInterval)
          handle = js.undefined
        }
      )
    )

    el
  }

  private def colorsFor(t: Theme, v: Variant): (Color, Color, Color) = v match {
    case Variant.Success => (t.successSoft, t.success, t.successBorder)
    case Variant.Running => (t.infoSoft, t.info, t.infoBorder)
    case Variant.Queued  => (t.surfaceDim, t.textMuted, t.border)
    case Variant.Warning => (t.warningSoft, t.warning, t.warningBorder)
    case Variant.Info    => (t.infoSoft, t.info, t.infoBorder)
    case Variant.Brand   => (t.successSoft, t.success, t.successBorder)
    case Variant.Neutral => (t.surfaceDim, t.textMuted, t.border)
  }
}
