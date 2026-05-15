package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Collapsible private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
}

/** Open/close container with no title bar. Pair with your own toggle button. For a
  * title-bar-included disclosure, use `Accordion`. */
object Collapsible extends ComponentFactory[Collapsible] {

  val open = Prop.inOut[Boolean, Collapsible](_.openVar)

  def body(content: Modifier[HtmlElement]*): Mod[Collapsible] = el =>
    el.bodySlot.amend(content*)

  override protected def build: Collapsible = {
    val bodySlot = div()
    val root = div(bodySlot)
    val el = new Collapsible(root, bodySlot)

    bodySlot.amend(
      el.openVar.signal.styled { (_, o) =>
        if (o) css.display(Display.Block) else css.display(Display.None)
      }
    )

    el
  }
}
