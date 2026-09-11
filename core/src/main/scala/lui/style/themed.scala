package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier

/** Build a Laminar `Modifier` from `f` applied to the current theme. Re-resolves when
  * `Theme.signal` changes, and merges with any other style modifier on the element. */
def themed(f: Theme => Style): Modifier[HtmlElement] =
  StyleLayers.dynamic(Theme.signal.map(f))
