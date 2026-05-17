package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom

final class Field private[components] (
    val root: HtmlElement,
    private[components] val controlSlot: HtmlElement,
    private[components] val msgId: String
) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val hintVar: Var[String] = Var("")
  private[components] val errorVar: Var[String] = Var("")
  private[components] val requiredVar: Var[Boolean] = Var(false)

  /** True when the field is currently in an error state (i.e. `error` is
    * non-empty). Useful when the slotted control wants to mirror its own
    * `invalid` prop: `TextInput.invalid <-- field.errors`. */
  val errors: Signal[Boolean] = errorVar.signal.map(_.nonEmpty)
}

object Field extends ComponentFactory[Field] {

  val label = Prop.in[String, Field](_.labelVar)
  val hint = Prop.in[String, Field](_.hintVar)
  val error = Prop.in[String, Field](_.errorVar)
  val required = Prop.in[Boolean, Field](_.requiredVar)

  def control(content: Modifier[HtmlElement]*): Mod[Field] = el =>
    el.controlSlot.amend(content*)

  private val nextId: () => String = {
    var n = 0
    () => { n += 1; s"lui-field-msg-$n" }
  }

  override protected def build: Field = {
    val msgId = nextId()
    val controlSlot = div()
    val root = div()
    val el = new Field(root, controlSlot, msgId)

    root.amend(
      stack.col(spacing.xs),
      div(
        stack.row(spacing.xs),
        span(typo.label, child.text <-- el.labelVar.signal),
        span(
          themed(t => css.color(t.danger) ++ css.fontWeight(FontWeight.Bold)),
          child.text <-- el.requiredVar.signal.map(if (_) "*" else "")
        )
      ),
      controlSlot,
      div(
        idAttr := msgId,
        Signal.combine(el.hintVar.signal, el.errorVar.signal).styled { case (t, (_, e)) =>
          if (e.nonEmpty) css.color(t.danger) ++ css.fontSize(fontSizes.md)
          else css.color(t.textSubtle) ++ css.fontSize(fontSizes.sm)
        },
        child.text <-- Signal.combine(el.hintVar.signal, el.errorVar.signal).map {
          case (h, e) => if (e.nonEmpty) e else h
        }
      ),
      // After the slot is populated, walk its DOM for the first focusable
      // form control and bind aria-describedby + aria-invalid on it.
      onMountCallback { _ =>
        val node = findControl(controlSlot.ref)
        if (node != null) {
          node.setAttribute("aria-describedby", msgId)
        }
      },
      // Track error state for aria-invalid on the slotted control.
      el.errors --> Observer[Boolean] { isErr =>
        val node = findControl(controlSlot.ref)
        if (node != null) {
          if (isErr) node.setAttribute("aria-invalid", "true")
          else node.removeAttribute("aria-invalid")
        }
      }
    )

    controlSlot.amend(stack.col(spacing.xs))

    el
  }

  // Find the first <input>/<select>/<textarea>/role=… inside the slot.
  private def findControl(root: dom.Element): dom.HTMLElement = {
    val selectors = "input, select, textarea, [role='combobox'], [role='spinbutton'], [role='slider'], [role='switch'], [role='checkbox']"
    root.querySelector(selectors) match {
      case el: dom.HTMLElement => el
      case _                   => null
    }
  }
}
