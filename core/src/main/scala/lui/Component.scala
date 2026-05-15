package lui

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.RenderableNode

/** A LUI component wraps a Laminar `HtmlElement` and any reactive state its props touch.
  * Extending `Modifier[HtmlElement]` lets a component slot directly into a Laminar tree
  * via the modifier pipeline; the `RenderableNode` instance below lets it appear inside
  * `children <-- ...` and similar Laminar binders. */
trait Component extends Modifier[HtmlElement] {
  def root: HtmlElement
  override def apply(into: HtmlElement): Unit = root(into)

  /** Hover / focus / pressed state tied to this component's root. Lazy: only initialized
    * (and only attaches listeners) on first access, so non-interactive components pay
    * nothing for it. Use `el.interact.state` or `el.interact.hovered.signal` etc. */
  lazy val interact: Interactive = Interactive.on(root)
}

object Component {
  given renderableComponent[C <: Component]: RenderableNode[C] =
    RenderableNode((c: C) => c.root)
}

/** A modifier applied to a component element. Side-effectful: typically wires a `Source`
  * into one of the element's `Var`s, or routes an `EventStream` to a `Sink`. */
type Mod[-El <: Component] = El => Unit

object Mod {
  inline def apply[El <: Component](f: El => Unit): Mod[El] = f
}

/** Abstract companion that owns the `apply(mods*)` factory. Subclasses implement `build` to
  * construct the element (root + reactive defaults already amended); `apply` folds user
  * `Mod`s onto it and returns. Removes the `mods.foreach(_(el)); el` tail from every
  * component's factory. */
abstract class ComponentFactory[El <: Component] {

  /** Construct the element with its root and default reactive bindings in place. */
  protected def build: El

  final def apply(mods: Mod[El]*): El = {
    val el = build
    mods.foreach(m => m(el))
    el
  }
}
