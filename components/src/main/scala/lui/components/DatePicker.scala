package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class DatePicker private[components] (
    val root: HtmlElement,
    private[components] val popover: Popover,
    private[components] val calendar: Calendar
) extends Component {
  private[components] val valueVar:       Var[Option[Day]] = Var(None)
  private[components] val placeholderVar: Var[String]      = Var("YYYY-MM-DD")
  private[components] val disabledVar:    Var[Boolean]     = Var(false)
  private[components] val widthVar:       Var[Length]      = Var(Length.px(180))
  private[components] val minVar:         Var[Option[Day]] = Var(None)
  private[components] val maxVar:         Var[Option[Day]] = Var(None)
}

/** Click-to-open date selector. Trigger is a text-input-style button showing the current
  * value (or placeholder), and the popover hosts a [[Calendar]]. Selecting a day writes
  * back to `value` and closes the popover. */
object DatePicker extends ComponentFactory[DatePicker] {

  val value       = Prop.inOut[Option[Day], DatePicker](_.valueVar)
  val placeholder = Prop.in[String,         DatePicker](_.placeholderVar)
  val disabled    = Prop.in[Boolean,        DatePicker](_.disabledVar)
  val width       = Prop.in[Length,         DatePicker](_.widthVar)
  val min         = Prop.in[Option[Day],    DatePicker](_.minVar)
  val max         = Prop.in[Option[Day],    DatePicker](_.maxVar)

  override protected def build: DatePicker = {
    val popover = Popover()
    val calendar = Calendar(
      Calendar.bordered := false
    )
    val el = new DatePicker(popover.root, popover, calendar)

    // ── trigger ────────────────────────────────────────────────────────────
    val triggerBtn = button(typ := "button")
    val interact = Interactive.on(triggerBtn)

    triggerBtn.amend(
      Signal.combine(el.disabledVar.signal, el.widthVar.signal, interact.state).styled {
        case (t, (d, w, i)) =>
          val borderColor =
            if (d) t.border
            else if (i.hovered) t.borderActive
            else t.border
          stack.between(spacing.sm) ++
            css.width(w) ++
            css.padding(spacing.md, spacing.lg) ++
            css.borderRadius(radius.md) ++
            css.border(Length.px(1.5), BorderStyle.Solid, borderColor) ++
            css.background(if (d) t.surfaceDim else t.surface) ++
            css.color(if (d) t.textSubtle else t.text) ++
            css.fontSize(fontSizes.xl) ++
            css.cursor(if (d) "not-allowed" else "pointer") ++
            css.raw("font-family", "inherit") ++
            css.raw("text-align", "left")
      },
      aria.disabled <-- el.disabledVar.signal,
      htmlDisabledAttr <-- el.disabledVar.signal,
      span(
        el.valueVar.signal.styled { (t, opt) =>
          css.raw("flex", "1 1 auto") ++ css.raw("min-width", "0") ++ css.ellipsis ++
            css.color(if (opt.isEmpty) t.textSubtle else t.text)
        },
        child.text <-- el.valueVar.signal.combineWith(el.placeholderVar.signal).map {
          case (Some(d), _)   => d.iso
          case (None,    plh) => plh
        }
      ),
      div(
        ThemedStyle(t =>
          css.width(Length.px(16)) ++ css.height(Length.px(16)) ++ css.color(t.textMuted)
        ),
        icons.calendar
      )
    )

    // ── wire popover ───────────────────────────────────────────────────────
    Popover.trigger(triggerBtn)(popover)
    Popover.body(calendar)(popover)

    // Two-way value sync between Calendar and DatePicker.
    el.root.amend(
      // down: parent's value into calendar
      el.valueVar.signal --> calendar.valueVar.writer,
      // up: calendar selection bubbles up, and closes the popover.
      calendar.valueVar.signal.changes
        .filter(_.isDefined)
        .distinct --> Observer[Option[Day]] { sel =>
          el.valueVar.set(sel)
          popover.openVar.set(false)
        },
      // When the popover opens, jump the calendar to the month of the current value
      // (or today's month) so the user lands on something useful.
      popover.openVar.signal.changes.filter(identity)
        .compose(_.withCurrentValueOf(el.valueVar.signal))
        .map { case (_, opt) =>
          opt.map(_.firstOfMonth).getOrElse(Day.today.firstOfMonth)
        } --> calendar.monthVar.writer,
      // Pass through min/max bounds.
      el.minVar.signal --> calendar.minVar.writer,
      el.maxVar.signal --> calendar.maxVar.writer
    )

    el
  }

  // Convenience handles for callers that want to disable bookkeeping via the
  // underlying form-disabled attribute. Aliased here to avoid colliding with
  // the local `disabled` prop val.
  private val htmlDisabledAttr = com.raquo.laminar.api.L.disabled
}
