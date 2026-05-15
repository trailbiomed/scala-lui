package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier

/** Build a Laminar `Modifier` that binds the inline `style` attribute to the result of `f`
  * applied to the current theme. Re-renders when `Theme.signal` changes. */
def themed(f: Theme => Style): Modifier[HtmlElement] =
  styleAttr <-- Theme.signal.map(t => f(t).toCss)
