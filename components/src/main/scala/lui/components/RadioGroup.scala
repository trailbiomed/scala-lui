package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class RadioGroup private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val optionsVar: Var[Seq[(String, String)]] = Var(Seq.empty)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val orientationVar: Var[RadioGroup.Orientation] = Var(RadioGroup.Orientation.Vertical)
}

/** Group of mutually-exclusive radio options.
  *
  * Keyboard: Tab onto the selected option; Arrow keys move and select between
  * options; Home/End jump to the first/last enabled option. Uses roving
  * tabIndex so only the selected option is in the page's tab order, per the
  * WAI radio-group pattern. */
object RadioGroup extends ComponentFactory[RadioGroup] {

  enum Orientation { case Vertical, Horizontal }

  val value = Prop.inOut[String, RadioGroup](_.valueVar)
  val options = Prop.in[Seq[(String, String)], RadioGroup](_.optionsVar)
  val disabled = Prop.in[Boolean, RadioGroup](_.disabledVar)
  val orientation = Prop.in[Orientation, RadioGroup](_.orientationVar)

  private val dotSize: Length = Length.px(16)

  override protected def build: RadioGroup = {
    val root = div()
    val el = new RadioGroup(root)

    root.amend(
      role := "radiogroup",
      el.orientationVar.signal.styled { (_, o) =>
        o match {
          case Orientation.Vertical   => stack.col(spacing.md)
          case Orientation.Horizontal => stack.row(spacing.xl) ++ stack.wrap
        }
      },
      AriaExtras.ariaOrientation <-- el.orientationVar.signal.map {
        case Orientation.Vertical   => "vertical"
        case Orientation.Horizontal => "horizontal"
      },
      children <-- el.optionsVar.signal.map { opts =>
        opts.map { case (key, lbl) => optionEl(key, lbl, el) }.toList
      },
      // Arrow keys + Home/End at the group level. They move the focus AND
      // change the bound value (WAI: radio group selection follows focus).
      onKeyDown --> Observer[dom.KeyboardEvent] { ev =>
        if (!el.disabledVar.now()) {
          val opts = el.optionsVar.now().map(_._1).toVector
          if (opts.nonEmpty) {
            val current = el.valueVar.now()
            val idx = opts.indexOf(current)
            def setAt(n: Int): Unit = {
              val clamped = ((n % opts.length) + opts.length) % opts.length
              val key = opts(clamped)
              el.valueVar.set(key)
              // Move focus to the matching option button.
              val target = root.ref.querySelector(s"[role='radio'][data-key='${cssEscape(key)}']")
              target match {
                case h: dom.HTMLElement =>
                  val _ = h.asInstanceOf[scala.scalajs.js.Dynamic]
                    .focus(scala.scalajs.js.Dynamic.literal(preventScroll = false))
                case _ => ()
              }
            }
            ev.key match {
              case "ArrowDown" | "ArrowRight" =>
                ev.preventDefault(); setAt(if (idx < 0) 0 else idx + 1)
              case "ArrowUp" | "ArrowLeft" =>
                ev.preventDefault(); setAt(if (idx < 0) opts.length - 1 else idx - 1)
              case "Home" =>
                ev.preventDefault(); setAt(0)
              case "End" =>
                ev.preventDefault(); setAt(opts.length - 1)
              case _ => ()
            }
          }
        }
      }
    )

    el
  }

  private def cssEscape(s: String): String =
    // Naive — covers the common case where keys are alphanumeric/`-`/`_`. If the
    // caller hands us a quote or a backslash we just strip it (the data-key
    // attribute would otherwise need full escaping).
    s.filter(c => c != '\'' && c != '\\' && c != '"')

  private def optionEl(key: String, lbl: String, el: RadioGroup): HtmlElement = {
    val root = button(typ := "button")
    val interact = Interactive.on(root)
    val isSelected: Signal[Boolean] = el.valueVar.signal.map(_ == key)

    root.amend(
      role := "radio",
      dataAttr("key") := key,
      aria.checked <-- isSelected.map(_.toString),
      aria.disabled <-- el.disabledVar.signal,
      // Roving tabindex: selected option is tab-reachable; the rest -1. If no
      // option is selected, the first becomes the tab stop so users can enter
      // the group with Tab.
      tabIndex <-- Signal
        .combine(isSelected, el.valueVar.signal, el.optionsVar.signal)
        .map { case (sel, cur, opts) =>
          if (sel) 0
          else if (cur.isEmpty && opts.headOption.map(_._1).contains(key)) 0
          else -1
        },
      Signal
        .combine(isSelected, el.disabledVar.signal, interact.state)
        .styled { case (t, (selected, disabled, i)) =>
          val ring =
            if (i.focused && !i.pressed && !disabled)
              css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.3).toCss}")
            else css.raw("box-shadow", "none")
          stack.row(spacing.md) ++
            css.alignItems("center") ++
            css.padding(Length.px(2)) ++
            css.background(Color.transparent) ++
            css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
            css.borderRadius(radius.sm) ++
            css.cursor(if (disabled) "not-allowed" else "pointer") ++
            css.opacity(if (disabled) 0.55 else 1.0) ++
            css.raw("font-family", "inherit") ++
            css.raw("text-align", "left") ++
            css.raw("user-select", "none") ++
            css.raw("outline", "none") ++
            ring
        },
      // Visual dot
      span(
        isSelected.styled { (t, sel) =>
          stack.centerAll ++
            css.width(dotSize) ++ css.height(dotSize) ++
            css.borderRadius(radius.pill) ++
            css.border(Length.px(1.5), BorderStyle.Solid, if (sel) t.brand else t.border) ++
            css.background(t.surface) ++
            css.transition("border-color", 120) ++
            stack.noShrink
        },
        child.maybe <-- isSelected.map { sel =>
          if (sel) Some(span(themed(t =>
            css.width(Length.px(8)) ++ css.height(Length.px(8)) ++
              css.borderRadius(radius.pill) ++ css.background(t.brand)
          )))
          else None
        }
      ),
      // Label
      span(typo.body, lbl),
      onClick.preventDefault.mapTo(key).filter(_ => !el.disabledVar.now()) -->
        el.valueVar.writer
    )
    root
  }
}
