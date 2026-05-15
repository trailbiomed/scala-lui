package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier

/** A Style that depends on the current `Theme`. Doubles as a Laminar `Modifier[HtmlElement]`
  * so it can be dropped into any tag directly:
  *
  * {{{
  *   div(typo.h1, "Workbench")                  // theme-only
  *   div(typo.h1 ++ css.margin(Length.px(0)))   // themed + static
  *   div(surface.card ++ stack.col(spacing.lg)) // themed + static layout
  * }}}
  *
  * Subscribes to `Theme.signal` on mount; reruns when the theme changes. */
final class ThemedStyle(val resolve: Theme => Style) extends Modifier[HtmlElement] {

  def ++(other: ThemedStyle): ThemedStyle =
    ThemedStyle(t => resolve(t) ++ other.resolve(t))

  /** Static decls layered on top of the themed ones. */
  def ++(other: Style): ThemedStyle =
    ThemedStyle(t => resolve(t) ++ other)

  override def apply(el: HtmlElement): Unit = {
    el.amend(styleAttr <-- Theme.signal.map(t => resolve(t).toCss))
  }
}

object ThemedStyle {
  def apply(f: Theme => Style): ThemedStyle = new ThemedStyle(f)
  val empty: ThemedStyle = new ThemedStyle(_ => Style.empty)
}

/** Static-first composition with a themed tail. */
extension (s: Style) {
  def ++(other: ThemedStyle): ThemedStyle = ThemedStyle(t => s ++ other.resolve(t))
}

/** Bind a state Signal AND the current Theme into a reactive inline style. */
extension [A](sig: Signal[A]) {
  def styled(f: (Theme, A) => Style): Modifier[HtmlElement] =
    styleAttr <-- Signal.combine(Theme.signal, sig).map { case (t, a) => f(t, a).toCss }
}
