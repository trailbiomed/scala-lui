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

  /** True when the user has requested reduced motion via the OS. Components that animate
    * (Spinner, Modal/Drawer slide, Tooltip fade, Toast fade) gate transitions and intervals
    * off this signal. `StrictSignal` so callers can read `.now()` from imperative
    * code (e.g. inside an `onMountCallback`). */
  val reducedMotion: StrictSignal[Boolean] = {
    val mq = dom.window.matchMedia("(prefers-reduced-motion: reduce)")
    val v  = Var(mq.matches)
    mq.addEventListener(
      "change",
      (_: dom.Event) => v.set(mq.matches)
    )
    v.signal
  }
}
