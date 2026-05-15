package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Stat private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val valueVar: Var[String] = Var("")
  private[components] val unitVar: Var[String] = Var("")
  private[components] val hintVar: Var[String] = Var("")
  private[components] val trendVar: Var[Stat.Trend] = Var(Stat.Trend.None)
}

/** Big-number summary tile. */
object Stat extends ComponentFactory[Stat] {

  enum Trend { case Up, Down, None }

  val label = Prop.in[String, Stat](_.labelVar)
  val value = Prop.in[String, Stat](_.valueVar)
  val unit = Prop.in[String, Stat](_.unitVar)
  val hint = Prop.in[String, Stat](_.hintVar)
  val trend = Prop.in[Trend, Stat](_.trendVar)

  override protected def build: Stat = {
    val root = div()
    val el = new Stat(root)

    root.amend(
      stack.col(spacing.xs),
      div(typo.eyebrow, child.text <-- el.labelVar.signal),
      div(
        stack.row(spacing.xs),
        span(
          themed(t =>
            css.fontSize(Length.px(28)) ++
              css.fontWeight(FontWeight.SemiBold) ++
              css.color(t.text) ++
              css.raw("line-height", "1")
          ),
          child.text <-- el.valueVar.signal
        ),
        span(
          typo.muted,
          child.text <-- el.unitVar.signal
        ),
        span(
          el.trendVar.signal.styled { (t, tr) =>
            val (color, vis) = tr match {
              case Trend.Up   => (t.success, true)
              case Trend.Down => (t.danger, true)
              case Trend.None => (t.textSubtle, false)
            }
            css.color(color) ++
              css.fontSize(fontSizes.lg) ++
              css.opacity(if (vis) 1.0 else 0.0)
          },
          child.text <-- el.trendVar.signal.map {
            case Trend.Up   => "▲"
            case Trend.Down => "▼"
            case Trend.None => ""
          }
        )
      ),
      div(typo.hint, child.text <-- el.hintVar.signal)
    )

    el
  }
}
