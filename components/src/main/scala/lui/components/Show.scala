package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

/** Persistent show/hide wrapper. Mounts its content **once** and
  * toggles between `display: contents` (transparent — children
  * inherit the parent's layout context) and `display: none` based on
  * the `visible` prop.
  *
  * Use this in place of a `child <-- viewSignal.map(buildPanel)`
  * binding when you need the wrapped subtree to keep its
  * **identity** across visibility changes:
  *
  *   - Child Components retain their internal `Var`s (sort order,
  *     scroll position, draft form text), not just whatever the
  *     parent re-feeds via Props.
  *   - Subscriptions stay alive — useful when the panel owns a
  *     polling stream, a websocket, or a long-running interval that
  *     you don't want to tear down on every nav.
  *   - Third-party widgets that resist remount (canvas / WebGL /
  *     embedded iframes) stay attached.
  *
  * Cost: every wrapped panel is mounted from app boot. Its data
  * fetches fire, its intervals tick, its DOM exists in memory.
  * For the lift-state-up-instead default, see the skill doc.
  *
  * `display: contents` makes the wrapper itself disappear from the
  * box tree when visible, so wrapping a flex/grid child doesn't add
  * an extra "anonymous flex item" between the parent and the panel.
  *
  * Example:
  *
  * {{{
  *   div(
  *     stack.col(spacing.lg),
  *     Show(
  *       Show.visible <-- viewVar.signal.map(_ == View.Explore),
  *       Show.content(ExplorePanel(... bindings ...)),
  *     ),
  *     Show(
  *       Show.visible <-- viewVar.signal.map(_ == View.Analyses),
  *       Show.content(AnalysesPanel(... bindings ...)),
  *     ),
  *   )
  * }}}
  */
final class Show private[components] (val root: HtmlElement) extends Component {
  private[components] val visibleVar: Var[Boolean] = Var(true)
}

object Show extends ComponentFactory[Show] {

  /** Visibility toggle. `true` → wrapper renders as `display:
    * contents` (transparent); `false` → `display: none` (hidden,
    * but content stays mounted). */
  val visible = Prop.in[Boolean, Show](_.visibleVar)

  /** Content slot. Modifiers are amended to the wrapper element once
    * at build time and stay mounted across visibility flips. */
  def content(mods: Modifier[HtmlElement]*): Mod[Show] = el =>
    el.root.amend(mods*)

  override protected def build: Show = {
    val root = div()
    val el = new Show(root)
    root.amend(
      el.visibleVar.signal.styled { (_, v) =>
        css.display(if (v) Display.Contents else Display.None)
      },
    )
    el
  }
}
