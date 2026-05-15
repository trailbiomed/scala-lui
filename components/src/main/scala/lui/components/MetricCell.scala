package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class MetricCell private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val scoreVar: Var[String] = Var("")
  private[components] val barVar: Var[Option[Double]] = Var(None)
  private[components] val stateVar: Var[MetricCell.State] = Var(MetricCell.State.Idle)
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]
}

object MetricCell extends ComponentFactory[MetricCell] {

  enum State { case Idle, Active, Running, Queued }

  val value = Prop.in[String, MetricCell](_.valueVar)
  val score = Prop.in[String, MetricCell](_.scoreVar)
  val bar = Prop.in[Option[Double], MetricCell](_.barVar)
  val state = Prop.in[State, MetricCell](_.stateVar)
  val click = Prop.out[Unit, MetricCell](_.clickBus)

  override protected def build: MetricCell = {
    val root = div()
    val el = new MetricCell(root)

    val barTrackStyle = themed(t =>
      css.height(Length.px(3)) ++
        css.borderRadius(radius.pill) ++
        css.background(t.border) ++
        css.overflow("hidden") ++
        css.width(Length.px(36))
    )
    val barFillFor: (Theme, Double) => Style = (t, fraction) => {
      val pct = math.max(0.0, math.min(1.0, fraction)) * 100
      css.height(Length.pct(100)) ++
        css.background(t.brand) ++
        css.borderRadius(radius.pill) ++
        css.raw("width", s"${pct}%")
    }

    root.amend(
      Signal.combine(el.stateVar.signal, el.interact.state).styled { case (t, (s, i)) =>
        val hovered = i.hovered
        val (bg, bd, shadow) = (s, hovered) match {
          case (State.Active, _) =>
            (t.brandSoft, t.brand, s"0 0 0 3px ${t.brand.alpha(0.18).toCss}")
          case (State.Running, _) =>
            (t.infoSoft, t.infoBorder, "none")
          case (State.Queued, _) =>
            (t.surface, t.border, "none")
          case (State.Idle, true) =>
            (t.surface, t.borderActive, "none")
          case (State.Idle, false) =>
            (t.surface, t.border, "none")
        }
        stack.col(Length.px(3)) ++
          css.alignItems("center") ++
          css.padding(spacing.md, Length.px(10)) ++
          css.borderRadius(radius.lg) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.background(bg) ++
          css.minWidth(Length.px(58)) ++
          css.transition("border-color", 150) ++
          css.cursor(if (s == State.Queued) "default" else "pointer") ++
          css.opacity(if (s == State.Queued) 0.45 else 1.0) ++
          css.raw("box-shadow", shadow)
      },
      onClick.mapToUnit.filter(_ => el.stateVar.now() != State.Queued) --> el.clickBus.writer,
      div(
        el.stateVar.signal.styled { (t, s) =>
          val c = if (s == State.Active) t.brand else t.text
          css.fontSize(fontSizes.xl) ++ css.fontWeight(FontWeight.Bold) ++ css.color(c)
        },
        child.text <-- el.valueVar.signal
      ),
      div(
        el.stateVar.signal.styled { (t, s) =>
          val c = if (s == State.Active) t.brand else t.textSubtle
          css.fontSize(Length.px(10)) ++
            css.fontWeight(if (s == State.Active) FontWeight.SemiBold else FontWeight.Regular) ++
            css.color(c)
        },
        child.text <-- el.scoreVar.signal
      ),
      child.maybe <-- Signal.combine(Theme.signal, el.barVar.signal).map { case (t, opt) =>
        opt.map(fraction =>
          div(
            barTrackStyle,
            div(barFillFor(t, fraction))
          )
        )
      }
    )

    el
  }
}
