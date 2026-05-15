package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Card private[components] (val root: HtmlElement) extends Component {
  private[components] val interactiveVar: Var[Boolean] = Var(false)
  private[components] val paddingVar: Var[Length] = Var(spacing.xl)
  private[components] val clickBus: EventBus[Unit] = new EventBus[Unit]
}

object Card extends ComponentFactory[Card] {

  val interactive = Prop.in[Boolean, Card](_.interactiveVar)

  val padding = Prop.in[Length, Card](_.paddingVar)

  val click = Prop.out[Unit, Card](_.clickBus)

  def children(content: Modifier[HtmlElement]*): Mod[Card] = el => el.root.amend(content*)

  override protected def build: Card = {
    val root = div()
    val el = new Card(root)

    root.amend(
      Signal.combine(el.interactiveVar.signal, el.paddingVar.signal, el.interact.state).styled {
        case (t, (interactiveOn, pad, i)) =>
          val bd = if (interactiveOn && i.hovered) t.borderActive else t.border
          surface.card.resolve(t) ++
            css.padding(pad) ++
            css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
            css.transition("border-color", 150) ++
            (if (interactiveOn) css.cursor("pointer") else Style.empty)
      },
      onClick.mapToUnit.filter(_ => el.interactiveVar.now()) --> el.clickBus.writer
    )
    el
  }
}
