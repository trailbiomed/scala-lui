package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Calendar private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar:      Var[Option[Day]]    = Var(None)
  private[components] val monthVar:      Var[Day]            = Var(Day.today.firstOfMonth)
  private[components] val weekStartVar:  Var[Int]            = Var(1) // 0 = Sunday, 1 = Monday
  private[components] val minVar:        Var[Option[Day]]    = Var(None)
  private[components] val maxVar:        Var[Option[Day]]    = Var(None)
  private[components] val borderedVar:   Var[Boolean]        = Var(true)
  private[components] val disabledFnVar: Var[Day => Boolean] = Var((_: Day) => false)
  /** Day currently in the keyboard "focus ring" — drives roving tabindex and
    * keyboard navigation. Defaults to the selected day, or today, or the
    * first day of `month`. */
  private[components] val focusedDayVar: Var[Day] = Var(Day.today)
}

/** Month-grid date selector with keyboard navigation:
  *
  *  - ArrowLeft/Right move one day. ArrowUp/Down move one week.
  *  - PageUp/Down move one month (Shift+PageUp/Down move one year).
  *  - Home/End jump to the first/last day of the current week.
  *  - Enter or Space selects the focused day. */
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

    // Initialize focusedDay from value or today, then keep it within the
    // displayed month when the user navigates by month.
    root.amend(
      el.valueVar.signal.changes.collect { case Some(d) => d } --> el.focusedDayVar.writer,
      el.monthVar.signal.changes
        .withCurrentValueOf(el.focusedDayVar.signal)
        .filter { case (m, cur) => cur.month != m.month || cur.year != m.year }
        .map { case (m, _) => Day(m.year, m.month, 1) }
        --> el.focusedDayVar.writer
    )

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
        labels.map { lbl =>
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
            lbl
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
      el.disabledFnVar.signal,
      el.focusedDayVar.signal
    )

    val grid = div(
      role := "grid",
      AriaExtras.ariaOrientation := "horizontal",
      css.display(Display.Grid) ++
        css.raw("grid-template-columns", "repeat(7, 1fr)") ++
        css.gap(Length.px(2)) ++
        css.padding(spacing.xs),
      children <-- gridState.map { case (pivot, sel, ws, mn, mx, df, foc) =>
        renderGrid(pivot, sel, ws, mn, mx, df, foc, el)
      },
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        val cur = el.focusedDayVar.now()
        def setFocus(d: Day): Unit = {
          el.focusedDayVar.set(d)
          // If we cross month boundary, jump the displayed month too.
          val pivot = el.monthVar.now()
          if (d.year != pivot.year || d.month != pivot.month) {
            el.monthVar.set(Day(d.year, d.month, 1))
          }
        }
        ev.key match {
          case "ArrowLeft" =>
            ev.preventDefault(); setFocus(cur.addDays(-1))
          case "ArrowRight" =>
            ev.preventDefault(); setFocus(cur.addDays(1))
          case "ArrowUp" =>
            ev.preventDefault(); setFocus(cur.addDays(-7))
          case "ArrowDown" =>
            ev.preventDefault(); setFocus(cur.addDays(7))
          case "PageUp" if ev.shiftKey =>
            ev.preventDefault(); setFocus(cur.addMonths(-12))
          case "PageDown" if ev.shiftKey =>
            ev.preventDefault(); setFocus(cur.addMonths(12))
          case "PageUp" =>
            ev.preventDefault(); setFocus(cur.addMonths(-1))
          case "PageDown" =>
            ev.preventDefault(); setFocus(cur.addMonths(1))
          case "Home" =>
            ev.preventDefault()
            val dow = if (el.weekStartVar.now() == 0) cur.dayOfWeekSun else cur.dayOfWeekMon
            setFocus(cur.addDays(-dow))
          case "End" =>
            ev.preventDefault()
            val dow = if (el.weekStartVar.now() == 0) cur.dayOfWeekSun else cur.dayOfWeekMon
            setFocus(cur.addDays(6 - dow))
          case "Enter" | " " =>
            ev.preventDefault()
            val disabled =
              el.minVar.now().exists(_ > cur) ||
                el.maxVar.now().exists(_ < cur) ||
                el.disabledFnVar.now().apply(cur)
            if (!disabled) el.valueVar.set(Some(cur))
          case _ => ()
        }
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

  private def navButton(glyph: SvgElement, lbl: String, action: () => Unit): HtmlElement = {
    val btn = button(typ := "button")
    val interact = Interactive.on(btn)
    btn.amend(
      interact.state.styled { (t, i) =>
        val ring =
          if (i.focused && !i.pressed)
            css.raw("box-shadow", s"0 0 0 2px ${t.brand.alpha(0.35).toCss}")
          else css.raw("box-shadow", "none")
        stack.centerAll ++
          css.width(Length.px(28)) ++
          css.height(Length.px(28)) ++
          css.borderRadius(radius.sm) ++
          css.border(Length.px(1), BorderStyle.Solid, Color.transparent) ++
          css.background(if (i.hovered) t.surfaceDim else Color.transparent) ++
          css.color(t.textMuted) ++
          css.cursor("pointer") ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          ring
      },
      aria.label := lbl,
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
      focused: Day,
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
      val isFocused = d == focused
      dayCell(d, inMonth, isSel, isToday, disabled, isFocused, el)
    }.toList
  }

  private def dayCell(
      d: Day,
      inMonth: Boolean,
      selected: Boolean,
      isToday: Boolean,
      disabled: Boolean,
      isFocused: Boolean,
      el: Calendar
  ): HtmlElement = {
    val cell = button(typ := "button")
    val hovered = Var(false)
    val focused = Var(false)
    cell.amend(
      role := "gridcell",
      aria.selected := selected,
      aria.label := s"${Day.monthNames(d.month - 1)} ${d.day}, ${d.year}",
      tabIndex := (if (isFocused) 0 else -1),
      Signal.combine(hovered.signal, focused.signal).styled { case (t, (hv, foc)) =>
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
        val ring =
          if (foc && !disabled)
            css.raw("box-shadow", s"0 0 0 2px ${t.brand.alpha(0.4).toCss}")
          else css.raw("box-shadow", "none")
        stack.centerAll ++
          css.height(Length.px(32)) ++
          css.width(Length.pct(100)) ++
          css.borderRadius(radius.sm) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(if (selected || isToday) FontWeight.SemiBold else FontWeight.Regular) ++
          css.background(bg) ++
          css.color(fg) ++
          css.border(Length.px(1), BorderStyle.Solid, borderColor) ++
          css.opacity(if (disabled) 0.4 else 1.0) ++
          css.cursor(if (disabled) "default" else "pointer") ++
          css.raw("user-select", "none") ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          css.raw("padding", "0") ++
          ring
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onFocus.mapTo(true) --> focused.writer,
      onBlur.mapTo(false) --> focused.writer,
      onClick.preventDefault.mapToUnit --> Observer[Unit] { _ =>
        if (!disabled) {
          el.focusedDayVar.set(d)
          el.valueVar.set(Some(d))
        }
      },
      d.day.toString
    )
    cell
  }
}
