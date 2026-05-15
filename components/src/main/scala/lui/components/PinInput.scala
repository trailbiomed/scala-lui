package lui.components

import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class PinInput private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val lengthVar: Var[Int] = Var(4)
  private[components] val maskVar: Var[Boolean] = Var(false)
}

/** Fixed-length code entry split into N single-character inputs. */
object PinInput extends ComponentFactory[PinInput] {

  val value = Prop.inOut[String, PinInput](_.valueVar)
  val length = Prop.in[Int, PinInput](_.lengthVar)
  val mask = Prop.in[Boolean, PinInput](_.maskVar)

  override protected def build: PinInput = {
    val root = div()
    val el = new PinInput(root)

    root.amend(
      stack.row(spacing.sm),
      children <-- Signal.combine(el.lengthVar.signal, el.maskVar.signal).map {
        case (n, masked) =>
          (0 until n).toList.map(i => cell(i, n, masked, el.valueVar))
      }
    )
    el
  }

  private def cell(idx: Int, total: Int, masked: Boolean, v: Var[String]): HtmlElement = {
    val focused = Var(false)
    input(
      typ := (if (masked) "password" else "text"),
      maxLength := 1,
      htmlValue <-- v.signal.map(s => if (idx < s.length) s.substring(idx, idx + 1) else ""),
      focused.signal.styled { (t, f) =>
        val bd = if (f) t.borderActive else t.border
        css.width(Length.px(40)) ++
          css.height(Length.px(48)) ++
          css.textAlign(TextAlign.Center) ++
          css.fontSize(Length.px(20)) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.color(t.text) ++
          css.background(t.surface) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.borderRadius(radius.md) ++
          css.raw("font-family", "inherit") ++
          css.raw("outline", "none") ++
          css.raw("box-sizing", "border-box") ++
          css.transition("border-color", 150)
      },
      onFocus.mapTo(true) --> focused.writer,
      onBlur.mapTo(false) --> focused.writer,
      // Intercept paste so a single Cmd-V / Ctrl-V fills every cell from this one onward.
      onPaste --> Observer[dom.ClipboardEvent] { ev =>
        ev.preventDefault()
        val pasted = Option(ev.clipboardData).map(_.getData("text")).getOrElse("")
        val cleaned = pasted.filter(c => !c.isWhitespace)
        if (cleaned.nonEmpty) {
          fill(idx, total, cleaned, v, ev.target.asInstanceOf[dom.HTMLInputElement])
        }
      },
      onInput --> Observer[dom.Event] { ev =>
        val tgt = ev.target.asInstanceOf[dom.HTMLInputElement]
        val raw = tgt.value
        // If autofill / browser-paste landed >1 char in this cell, distribute too.
        if (raw.length > 1) {
          fill(idx, total, raw, v, tgt)
        } else {
          val ch = raw.takeRight(1)
          val current = v.now()
          val padded = current.padTo(total, ' ').take(total)
          val updated = padded.updated(idx, if (ch.isEmpty) ' ' else ch.head)
          v.set(updated.takeWhile(_ != ' '))
          if (ch.nonEmpty && idx + 1 < total) {
            val parent = tgt.parentNode.asInstanceOf[dom.HTMLElement]
            val next = parent.children.item(idx + 1).asInstanceOf[dom.HTMLInputElement]
            if (next != null) next.focus()
          }
        }
      }
    )
  }

  /** Distribute `chars` starting at cell `startIdx` and move focus to the last filled cell
    * (or the last cell, whichever is smaller). */
  private def fill(
      startIdx: Int,
      total: Int,
      chars: String,
      v: Var[String],
      anyCellInGroup: dom.HTMLInputElement
  ): Unit = {
    val current = v.now()
    val padded = current.padTo(total, ' ').take(total).toCharArray
    var i = startIdx
    var j = 0
    while (i < total && j < chars.length) {
      padded(i) = chars.charAt(j)
      i += 1
      j += 1
    }
    val newStr = padded.takeWhile(_ != ' ').mkString
    v.set(newStr)
    val parent = anyCellInGroup.parentNode.asInstanceOf[dom.HTMLElement]
    val focusIdx = math.min(i, total - 1)
    val nextEl = parent.children.item(focusIdx).asInstanceOf[dom.HTMLInputElement]
    if (nextEl != null) nextEl.focus()
  }
}
