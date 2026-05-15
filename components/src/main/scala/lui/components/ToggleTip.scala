package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ToggleTip private[components] (
    val root: HtmlElement,
    private[components] val popover: Popover
) extends Component {
  private[components] val labelVar: Var[String] = Var("")
}

/** Click-to-toggle small tip popover. Like `Tooltip` but stays open until clicked again
  * or outside. Useful on touch, where hover doesn't work. */
object ToggleTip extends ComponentFactory[ToggleTip] {

  val label = Prop.in[String, ToggleTip](_.labelVar)
  val placement = Prop.in[Popover.Placement, ToggleTip](el =>
    el.popover.placementVar
  )

  def trigger(content: Modifier[HtmlElement]*): Mod[ToggleTip] = el =>
    Popover.trigger(content*)(el.popover)

  override protected def build: ToggleTip = {
    val popover = Popover()
    val el = new ToggleTip(popover.root, popover)

    Popover.body(
      span(typo.body, child.text <-- el.labelVar.signal)
    )(popover)

    el
  }
}
