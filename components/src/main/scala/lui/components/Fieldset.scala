package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Fieldset private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val legendVar: Var[String] = Var("")
  private[components] val hintVar: Var[String] = Var("")
}

/** Group of related fields with a legend. */
object Fieldset extends ComponentFactory[Fieldset] {

  val legend = Prop.in[String, Fieldset](_.legendVar)
  val hint = Prop.in[String, Fieldset](_.hintVar)

  def body(content: Modifier[HtmlElement]*): Mod[Fieldset] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Fieldset = {
    val bodySlot = div()
    val root = div()
    val el = new Fieldset(root, bodySlot)

    root.amend(
      stack.col(spacing.md),
      div(
        stack.col(spacing.xs),
        span(typo.eyebrow, child.text <-- el.legendVar.signal),
        span(typo.hint, child.text <-- el.hintVar.signal)
      ),
      bodySlot
    )

    bodySlot.amend(stack.col(spacing.md))
    el
  }
}
