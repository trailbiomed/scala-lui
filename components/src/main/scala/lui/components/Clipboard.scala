package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import org.scalajs.dom
import scala.scalajs.js

final class Clipboard private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[String] = Var("")
  private[components] val labelVar: Var[String] = Var("Copy")
  private[components] val copiedVar: Var[Boolean] = Var(false)
}

/** Button that copies a string to the clipboard. Briefly flips its label to "Copied".
  * Reuses `Button`. */
object Clipboard extends ComponentFactory[Clipboard] {

  val value = Prop.in[String, Clipboard](_.valueVar)
  val label = Prop.in[String, Clipboard](_.labelVar)

  override protected def build: Clipboard = {
    val root = div()
    val el = new Clipboard(root)

    val btn = Button(
      Button.label <-- Signal
        .combine(el.labelVar.signal, el.copiedVar.signal)
        .map { case (l, c) => if (c) "✓ Copied" else l },
      Button.variant := Button.Variant.Secondary,
      Button.size := Button.Size.Small,
      Button.click.foreach { _ =>
        val nav = dom.window.navigator.asInstanceOf[js.Dynamic]
        val cb = nav.clipboard
        if (cb != null && !js.isUndefined(cb)) {
          val _ = cb.writeText(el.valueVar.now())
        }
        el.copiedVar.set(true)
        val _ = js.timers.setTimeout(1200)(el.copiedVar.set(false))
      }
    )

    root.amend(btn)
    el
  }
}
