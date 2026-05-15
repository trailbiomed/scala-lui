package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class SectionLabel private[components] (val root: HtmlElement) extends Component {
  private[components] val textVar: Var[String] = Var("")
}

object SectionLabel extends ComponentFactory[SectionLabel] {

  val text = Prop.in[String, SectionLabel](_.textVar)

  override protected def build: SectionLabel = {
    val root = span()
    val el = new SectionLabel(root)

    root.amend(
      typo.eyebrow,
      child.text <-- el.textVar.signal
    )
    el
  }
}
