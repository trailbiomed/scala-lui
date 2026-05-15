package lui

import com.raquo.laminar.api.L.*
import org.scalajs.dom

enum InputMode {
  case Mouse, Touch
}

/** Global signals describing the device's input mode and viewport size. Used by components
  * that need to adapt their layout or affordance visibility. There is exactly one of each
  * per page; subscribe lazily from components that care. */
object Device {

  val inputMode: Signal[InputMode] = {
    val mq = dom.window.matchMedia("(hover: none)")
    val v = Var(if (mq.matches) InputMode.Touch else InputMode.Mouse)
    mq.addEventListener(
      "change",
      (_: dom.Event) => v.set(if (mq.matches) InputMode.Touch else InputMode.Mouse)
    )
    v.signal
  }

  val viewportWidth: Signal[Int] = {
    val v = Var(dom.window.innerWidth.toInt)
    dom.window.addEventListener(
      "resize",
      (_: dom.Event) => v.set(dom.window.innerWidth.toInt)
    )
    v.signal
  }
}
