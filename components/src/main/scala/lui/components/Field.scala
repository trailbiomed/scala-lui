package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Field private[components] (
    val root: HtmlElement,
    private[components] val controlSlot: HtmlElement
) extends Component {
  private[components] val labelVar: Var[String] = Var("")
  private[components] val hintVar: Var[String] = Var("")
  private[components] val errorVar: Var[String] = Var("")
  private[components] val requiredVar: Var[Boolean] = Var(false)
}

/** Label + control + hint/error scaffold. Drop any input into the `control` slot:
  *
  * {{{
  *   Field(
  *     Field.label := "Email",
  *     Field.hint := "We never share this.",
  *     Field.control(TextInput(TextInput.value <--> email))
  *   )
  * }}}
  */
object Field extends ComponentFactory[Field] {

  val label = Prop.in[String, Field](_.labelVar)
  val hint = Prop.in[String, Field](_.hintVar)
  val error = Prop.in[String, Field](_.errorVar)
  val required = Prop.in[Boolean, Field](_.requiredVar)

  def control(content: Modifier[HtmlElement]*): Mod[Field] = el =>
    el.controlSlot.amend(content*)

  override protected def build: Field = {
    val controlSlot = div()
    val root = div()
    val el = new Field(root, controlSlot)

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
        Signal.combine(el.hintVar.signal, el.errorVar.signal).styled { case (t, (_, e)) =>
          if (e.nonEmpty) css.color(t.danger) ++ css.fontSize(fontSizes.md)
          else css.color(t.textSubtle) ++ css.fontSize(fontSizes.sm)
        },
        child.text <-- Signal.combine(el.hintVar.signal, el.errorVar.signal).map {
          case (h, e) => if (e.nonEmpty) e else h
        }
      )
    )

    controlSlot.amend(stack.col(spacing.xs))

    el
  }
}
