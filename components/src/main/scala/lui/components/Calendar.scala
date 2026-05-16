package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Calendar private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar:      Var[Option[Day]]    = Var(None)
  private[components] val monthVar:      Var[Day]            = Var(Day.today.firstOfMonth)
  private[components] val weekStartVar:  Var[Int]            = Var(1) // 0 = Sunday, 1 = Monday
  private[components] val minVar:        Var[Option[Day]]    = Var(None)
  private[components] val maxVar:        Var[Option[Day]]    = Var(None)
  private[components] val borderedVar:   Var[Boolean]        = Var(true)
  private[components] val disabledFnVar: Var[Day => Boolean] = Var((_: Day) => false)
}

/** Month-grid date selector. Pure presentation; doesn't open or close anything by itself —
  * for the trigger-and-popover behavior use [[DatePicker]] (which wraps a Calendar inside a
  * [[Popover]]).
  *
  *  - `value` is the selected date (`Option`, since "nothing selected" is valid).
  *  - `month` is the currently-displayed pivot. Two-way so the parent can navigate
  *    programmatically or react to user navigation.
  *  - `weekStart` is 0 (Sunday-first) or 1 (Monday-first, default).
  *  - `min` / `max` clamp the selectable range; `disabledFn` is a free-form predicate
  *    on top of that (e.g. "no weekends"). */
object Calendar extends ComponentFactory[Calendar] {

  val value      = Prop.inOut[Option[Day], Calendar](_.valueVar)
  val month      = Prop.inOut[Day,         Calendar](_.monthVar)
  val weekStart  = Prop.in[Int,            Calendar](_.weekStartVar)
  val min        = Prop.in[Option[Day],    Calendar](_.minVar)
  val max        = Prop.in[Option[Day],    Calendar](_.maxVar)
  val bordered   = Prop.in[Boolean,        Calendar](_.borderedVar)
  val disabledFn = Prop.in[Day => Boolean, Calendar](_.disabledFnVar)

  override protected def build: Calendar = {
    val root = div()
    val el = new Calendar(root)

    val prevBtn = navButton(icons.chevronLeft, "Previous month", () =>
      el.monthVar.set(el.monthVar.now().addMonths(-1))
    )
    val nextBtn = navButton(icons.chevronRight, "Next month", () =>
      el.monthVar.set(el.monthVar.now().addMonths(1))
    )

    val header = div(
      stack.between(spacing.sm) ++ css.padding(spacing.xs, spacing.sm),
      prevBtn,
      span(
        ThemedStyle(t =>
          css.color(t.text) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.fontSize(fontSizes.lg)
        ),
        child.text <-- el.monthVar.signal.map(d => s"${Day.monthNames(d.month - 1)} ${d.year}")
      ),
      nextBtn
    )

    val weekdayRow = div(
      css.display(Display.Grid) ++
        css.raw("grid-template-columns", "repeat(7, 1fr)") ++
        css.gap(Length.px(2)) ++
        css.padding(Length.px(0), spacing.xs),
      children <-- el.weekStartVar.signal.map { ws =>
        val labels = if (ws == 0) Day.weekdaysSun else Day.weekdaysMon
        labels.map { label =>
          span(
            ThemedStyle(t =>
              css.color(t.textSubtle) ++
                css.fontSize(fontSizes.xs) ++
                css.fontWeight(FontWeight.Medium) ++
                css.textTransform("uppercase") ++
                css.letterSpacing(Length.px(0.5)) ++
                css.textAlign(TextAlign.Center) ++
                css.padding(Length.px(4), Length.px(0))
            ),
            label
          )
        }.toList
      }
    )

    val gridState = Signal.combine(
      el.monthVar.signal,
      el.valueVar.signal,
      el.weekStartVar.signal,
      el.minVar.signal,
      el.maxVar.signal,
      el.disabledFnVar.signal
    )

    val grid = div(
      css.display(Display.Grid) ++
        css.raw("grid-template-columns", "repeat(7, 1fr)") ++
        css.gap(Length.px(2)) ++
        css.padding(spacing.xs),
      children <-- gridState.map { case (pivot, sel, ws, mn, mx, df) =>
        renderGrid(pivot, sel, ws, mn, mx, df, el)
      }
    )

    root.amend(
      el.borderedVar.signal.styled { (t, bord) =>
        stack.col(Length.zero) ++
          css.background(t.surface) ++
          css.color(t.text) ++
          css.borderRadius(radius.md) ++
          css.width(Length.px(264)) ++
          (if (bord) css.border(Length.px(1), BorderStyle.Solid, t.border) else Style.empty)
      },
      header,
      weekdayRow,
      grid
    )

    el
  }

  // --- internals -----------------------------------------------------------

  private def navButton(glyph: SvgElement, label: String, action: () => Unit): HtmlElement = {
    val btn = button(typ := "button")
    val interact = Interactive.on(btn)
    btn.amend(
      interact.state.styled { (t, i) =>
        stack.centerAll ++
          css.width(Length.px(28)) ++
          css.height(Length.px(28)) ++
          css.borderRadius(radius.sm) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent) ++
          css.background(if (i.hovered) t.surfaceDim else Color.transparent) ++
          css.color(t.textMuted) ++
          css.cursor("pointer") ++
          css.raw("font-family", "inherit")
      },
      aria.label := label,
      onClick.preventDefault.mapToUnit --> Observer[Unit](_ => action()),
      div(css.width(Length.px(16)) ++ css.height(Length.px(16)), glyph)
    )
    btn
  }

  private def renderGrid(
      pivot: Day,
      selected: Option[Day],
      weekStart: Int,
      minD: Option[Day],
      maxD: Option[Day],
      isCustomDisabled: Day => Boolean,
      el: Calendar
  ): List[HtmlElement] = {
    val first = pivot.firstOfMonth
    val firstCol =
      if (weekStart == 0) first.dayOfWeekSun
      else first.dayOfWeekMon
    val gridStart = first.addDays(-firstCol)
    (0 until 42).map { i =>
      val d = gridStart.addDays(i)
      val inMonth = d.month == pivot.month && d.year == pivot.year
      val isSel = selected.contains(d)
      val isToday = d == Day.today
      val outOfRange = minD.exists(_ > d) || maxD.exists(_ < d)
      val disabled = outOfRange || isCustomDisabled(d)
      dayCell(d, inMonth, isSel, isToday, disabled, el)
    }.toList
  }

  private def dayCell(
      d: Day,
      inMonth: Boolean,
      selected: Boolean,
      isToday: Boolean,
      disabled: Boolean,
      el: Calendar
  ): HtmlElement = {
    val cell = div()
    val hovered = Var(false)
    cell.amend(
      hovered.signal.styled { (t, hv) =>
        val bg =
          if (disabled) Color.transparent
          else if (selected) t.brand
          else if (hv) t.surfaceDim
          else Color.transparent
        val fg =
          if (disabled) t.textSubtle
          else if (selected) t.onBrand
          else if (isToday) t.brand
          else if (inMonth) t.text
          else t.textSubtle
        val borderColor =
          if (isToday && !selected) t.brand
          else Color.transparent
        stack.centerAll ++
          css.height(Length.px(32)) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(if (selected || isToday) FontWeight.SemiBold else FontWeight.Regular) ++
          css.background(bg) ++
          css.color(fg) ++
          css.border(Length.px(1), BorderStyle.Solid, borderColor) ++
          css.opacity(if (disabled) 0.4 else 1.0) ++
          css.cursor(if (disabled) "default" else "pointer") ++
          css.raw("user-select", "none")
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onClick.mapToUnit --> Observer[Unit] { _ =>
        if (!disabled) el.valueVar.set(Some(d))
      },
      d.day.toString
    )
    cell
  }
}
