package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

/** Persistent show/hide wrapper. Mounts its content **once** and
  * toggles its visibility based on the `visible` prop, without
  * remounting the subtree. State, subscriptions and scroll position
  * inside the wrapped content are preserved across flips.
  *
  * Two modes (see [[Show.Mode]]):
  *
  *  - `Layout` (default) — uses `display: contents` / `display: none`.
  *    When hidden, the wrapper takes **no layout space**. Equivalent
  *    to the original Show behaviour.
  *  - `Visibility` — uses `visibility: hidden` + `pointer-events:
  *    none` when hidden. The wrapper still occupies layout space
  *    (height/width unchanged), it just isn't painted and doesn't
  *    catch clicks. Use this when you want the parent's layout to
  *    stay stable as Show flips (e.g. tab panels that should keep
  *    the page from jumping when their content height differs).
  *
  * Example:
  *
  * {{{
  *   Show(
  *     Show.visible <-- viewVar.signal.map(_ == View.Explore),
  *     Show.content(ExplorePanel(... bindings ...)),
  *   )
  * }}}
  */
final class Show private[components] (val root: HtmlElement) extends Component {
  private[components] val visibleVar: Var[Boolean] = Var(true)
  private[components] val modeVar: Var[Show.Mode] = Var(Show.Mode.Layout)
}

object Show extends ComponentFactory[Show] {

  enum Mode {

    /** `display: contents` ↔ `display: none`. Hidden subtree contributes nothing to layout. */
    case Layout

    /** `display: contents` ↔ `visibility: hidden; pointer-events: none`. Hidden subtree
      * still occupies layout space but isn't painted or interactive. */
    case Visibility
  }

  /** Visibility toggle. `true` → wrapper shows; `false` → wrapper hides via `mode`. */
  val visible = Prop.in[Boolean, Show](_.visibleVar)

  /** Which hide mechanism to use. Default `Mode.Layout` matches the
    * pre-existing `Show` behaviour. */
  val mode = Prop.in[Mode, Show](_.modeVar)

  /** Content slot. Modifiers are amended to the wrapper element once
    * at build time and stay mounted across visibility flips. */
  def content(mods: Modifier[HtmlElement]*): Mod[Show] = el =>
    el.root.amend(mods*)

  override protected def build: Show = {
    val root = div()
    val el = new Show(root)
    root.amend(
      Signal
        .combine(el.visibleVar.signal, el.modeVar.signal)
        .styled { case (_, (visible, mode)) =>
          (visible, mode) match {
            case (true, _) =>
              // visible: take no layout-shape of our own + clear any
              // prior visibility:hidden in case the mode flipped.
              css.display(Display.Contents) ++
                css.raw("visibility", "visible") ++
                css.pointerEvents("auto")
            case (false, Mode.Layout) =>
              css.display(Display.None)
            case (false, Mode.Visibility) =>
              css.display(Display.Contents) ++
                css.raw("visibility", "hidden") ++
                css.pointerEvents("none")
          }
        }
    )
    el
  }
}
