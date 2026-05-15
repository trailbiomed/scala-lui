package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import scala.scalajs.js
import scala.scalajs.js.timers.SetIntervalHandle

final class Spinner private[components] (val root: HtmlElement) extends Component {
  private[components] val sizeVar: Var[Length] = Var(Length.px(20))
}

/** A small rotating ring. Avoids CSS `@keyframes` (banned per the no-CSS rule) by driving
  * the rotation from a `setInterval` updating an angle Var. ~30fps. */
object Spinner extends ComponentFactory[Spinner] {

  val size = Prop.in[Length, Spinner](_.sizeVar)

  override protected def build: Spinner = {
    val root = div()
    val el = new Spinner(root)

    val angle: Var[Int] = Var(0)
    var handle: js.UndefOr[SetIntervalHandle] = js.undefined

    root.amend(
      Signal.combine(el.sizeVar.signal, angle.signal).styled { case (t, (sz, a)) =>
        css.raw("display", "inline-block") ++
          css.width(sz) ++
          css.height(sz) ++
          css.raw("border-radius", "50%") ++
          css.border(Length.px(2), BorderStyle.Solid, t.surfaceDim) ++
          css.raw("border-top-color", t.brand.toCss) ++
          css.raw("transform", s"rotate(${a}deg)")
      },
      onMountUnmountCallback(
        mount = _ => {
          handle = js.timers.setInterval(33.0) {
            angle.update(a => (a + 12) % 360)
          }
        },
        unmount = _ => {
          handle.foreach(js.timers.clearInterval)
          handle = js.undefined
        }
      )
    )

    el
  }
}
