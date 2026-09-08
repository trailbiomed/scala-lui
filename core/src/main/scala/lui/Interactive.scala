package lui

import com.raquo.laminar.api.L.{Mod as _, *}
import org.scalajs.dom

/** `focused` is raw DOM focus — a mouse click sets it. `focusVisible` narrows that to
  * focus the keyboard put there, which is what a focus ring should key off; backgrounds and
  * other "this is the current item" affordances stay on `focused`. Defaulted so existing
  * three-argument construction still compiles. */
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
  * `state` adds a fourth, derived flag: `focusVisible`, which folds in
  * `Device.keyboardMode`. Being derived rather than tracked is what makes the good case
  * work — put the mouse down, reach for the keyboard, and the ring appears on the element
  * that already has focus. */
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

  /** Narrow a plain focus signal to focus the keyboard put there — the `focusVisible`
    * equivalent for a component that tracks focus in its own `Var` (a native `<select>`,
    * a slider thumb, a calendar cell) rather than through `Interactive.state`. */
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
