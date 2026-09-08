package lui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js

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

  /** True while the keyboard is what the user is navigating with — the distinction CSS
    * calls `:focus-visible`. Components draw focus rings off this (via
    * `InteractionState.focusVisible`) so a mouse click doesn't ring.
    *
    * Any keydown counts except a bare modifier: arrows move focus in a menu, Enter opens
    * one, Escape closes it, and typing in a field then tabbing out is still keyboard use.
    * Any pointer press counts the other way, and on *down*, because focus lands on
    * pointerdown.
    *
    * Both listeners are on the capture phase so the mode is settled before any element's
    * own focus handler runs — a listener that resolves afterwards shows the ring for a
    * frame. */
  val keyboardMode: StrictSignal[Boolean] = {
    val v = Var(false)
    dom.document.addEventListener(
      "keydown",
      (ev: dom.Event) => {
        val k = ev.asInstanceOf[dom.KeyboardEvent].key
        if (k != "Shift" && k != "Alt" && k != "Control" && k != "Meta") v.set(true)
      },
      useCapture = true
    )
    val pointer: js.Function1[dom.Event, Unit] = _ => v.set(false)
    dom.document.addEventListener("pointerdown", pointer, useCapture = true)
    dom.document.addEventListener("mousedown", pointer, useCapture = true)
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
