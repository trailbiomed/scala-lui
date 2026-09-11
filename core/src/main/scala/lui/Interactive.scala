package lui

import com.raquo.laminar.api.L.{Mod as _, *}
import org.scalajs.dom

/** @param focused      raw DOM focus, which a mouse click also sets
  * @param focusVisible focus the keyboard put there. Draw focus rings off this one;
  *                     backgrounds and current-item affordances belong on `focused`. */
final case class InteractionState(
    hovered: Boolean,
    focused: Boolean,
    pressed: Boolean,
    focusVisible: Boolean = false
)

/** Bundles the three tracked interaction-state Vars and installs the listeners that update
  * them on the host element. Construct via `Interactive.on(root)`; the listeners
  * auto-install — no need to remember to add anything to `root.amend(...)`.
  *
  * Uses Pointer Events so the same code path handles mouse, touch, and pen. `hovered`
  * stays false on touch (gated on `pointerType == "mouse"`); a tap drives `pressed`.
  *
  * `state` derives a fourth flag, `focusVisible`, from these three plus
  * `Device.keyboardMode`. */
final class Interactive private (
    val hovered: Var[Boolean],
    val focused: Var[Boolean],
    val pressed: Var[Boolean]
) {
  val state: Signal[InteractionState] =
    Signal
      .combine(hovered.signal, focused.signal, pressed.signal, Device.keyboardMode)
      .map { case (h, f, p, kb) =>
        InteractionState(h, f, p, focusVisible = f && kb)
      }
}

object Interactive {

  /** `InteractionState.focusVisible` for a component that tracks focus in its own `Var`
    * rather than through [[Interactive.state]]. */
  def focusVisible(focused: Signal[Boolean]): Signal[Boolean] =
    Signal.combine(focused, Device.keyboardMode).map { case (f, kb) => f && kb }

  /** Construct an Interactive and install its pointer/focus listeners onto `host`. The
    * listeners attach for the element's lifetime. */
  def on(host: HtmlElement): Interactive = {
    val hovered = Var(false)
    val focused = Var(false)
    val pressed = Var(false)
    val mouseOnly: dom.PointerEvent => Boolean = ev => ev.pointerType == "mouse"
    host.amend(
      onPointerEnter.filter(mouseOnly).mapTo(true) --> hovered.writer,
      onPointerLeave.filter(mouseOnly).mapTo(false) --> hovered.writer,
      onFocus.mapTo(true) --> focused.writer,
      onBlur.mapTo(false) --> focused.writer,
      onPointerDown.mapTo(true) --> pressed.writer,
      onPointerUp.mapTo(false) --> pressed.writer,
      onPointerCancel.mapTo(false) --> pressed.writer,
      onPointerLeave.mapTo(false) --> pressed.writer
    )
    new Interactive(hovered, focused, pressed)
  }
}
