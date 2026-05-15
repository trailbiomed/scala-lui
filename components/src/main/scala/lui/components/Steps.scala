package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Steps private[components] (val root: HtmlElement) extends Component {
  private[components] val stepsVar: Var[Seq[String]] = Var(Seq.empty)
  private[components] val currentVar: Var[Int] = Var(0)
  private[components] val orientationVar: Var[Steps.Orientation] = Var(Steps.Orientation.Horizontal)
}

/** A progress indicator for multi-stage workflows. `steps` are the labels (in order);
  * `current` is the 0-indexed active step. Steps before `current` are marked done, the step
  * at `current` is active, and steps after are upcoming. Read-only — `current` is set by
  * the caller, not by clicking. */
object Steps extends ComponentFactory[Steps] {

  enum Orientation { case Horizontal, Vertical }

  val steps = Prop.in[Seq[String], Steps](_.stepsVar)
  val current = Prop.in[Int, Steps](_.currentVar)
  val orientation = Prop.in[Orientation, Steps](_.orientationVar)

  private val markerSize: Length = Length.px(26)

  override protected def build: Steps = {
    val root = div()
    val el = new Steps(root)

    root.amend(
      el.orientationVar.signal.styled { (_, o) =>
        o match {
          case Orientation.Horizontal => stack.row() ++ css.width(Length.pct(100))
          case Orientation.Vertical   => stack.col(spacing.lg)
        }
      },
      children <-- Signal
        .combine(el.stepsVar.signal, el.currentVar.signal, el.orientationVar.signal)
        .map { case (ss, cur, o) =>
          val n = ss.size
          ss.zipWithIndex.flatMap { case (lbl, idx) =>
            val phase =
              if (idx < cur) Phase.Done
              else if (idx == cur) Phase.Active
              else Phase.Upcoming
            val item = stepItem(idx + 1, lbl, phase, o)
            val connector =
              if (idx < n - 1) Some(connectorEl(phase, o))
              else None
            Seq(item) ++ connector.toSeq
          }.toList
        }
    )
    el
  }

  private enum Phase { case Done, Active, Upcoming }

  private def stepItem(num: Int, lbl: String, phase: Phase, o: Orientation): HtmlElement = {
    val container = o match {
      case Orientation.Horizontal => div(stack.row(spacing.md), stack.noShrink)
      case Orientation.Vertical   => div(stack.row(spacing.md))
    }

    val marker = div(
      themed(t =>
        stack.centerAll ++
          css.width(markerSize) ++ css.height(markerSize) ++
          css.borderRadius(radius.pill) ++
          css.fontSize(fontSizes.md) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.border(Length.px(2), BorderStyle.Solid, markerBorder(t, phase)) ++
          css.background(markerBg(t, phase)) ++
          css.color(markerFg(t, phase)) ++
          stack.noShrink
      ),
      phase match {
        case Phase.Done     => "✓"
        case Phase.Active   => num.toString
        case Phase.Upcoming => num.toString
      }
    )

    val label = span(
      themed(t =>
        css.fontSize(fontSizes.lg) ++
          css.fontWeight(
            phase match {
              case Phase.Active => FontWeight.SemiBold
              case _            => FontWeight.Medium
            }
          ) ++
          css.color(
            phase match {
              case Phase.Done     => t.text
              case Phase.Active   => t.text
              case Phase.Upcoming => t.textMuted
            }
          )
      ),
      lbl
    )

    container.amend(marker, label)
    container
  }

  private def connectorEl(prevPhase: Phase, o: Orientation): HtmlElement = {
    val done = prevPhase == Phase.Done
    o match {
      case Orientation.Horizontal =>
        div(
          themed(t =>
            stack.grow ++
              css.height(Length.px(2)) ++
              css.background(if (done) t.brand else t.border) ++
              css.margin(Length.px(0)) ++
              css.raw("margin-left", spacing.md.toCss) ++
              css.raw("margin-right", spacing.md.toCss)
          )
        )
      case Orientation.Vertical =>
        div(
          themed(t =>
            css.width(Length.px(2)) ++
              css.height(Length.px(20)) ++
              css.background(if (done) t.brand else t.border) ++
              css.raw("margin-left", s"calc(${markerSize.toCss} / 2 - 1px)")
          )
        )
    }
  }

  private def markerBg(t: Theme, p: Phase): Color = p match {
    case Phase.Done     => t.brand
    case Phase.Active   => t.surface
    case Phase.Upcoming => t.surface
  }
  private def markerFg(t: Theme, p: Phase): Color = p match {
    case Phase.Done     => t.onBrand
    case Phase.Active   => t.brand
    case Phase.Upcoming => t.textMuted
  }
  private def markerBorder(t: Theme, p: Phase): Color = p match {
    case Phase.Done     => t.brand
    case Phase.Active   => t.brand
    case Phase.Upcoming => t.border
  }
}
