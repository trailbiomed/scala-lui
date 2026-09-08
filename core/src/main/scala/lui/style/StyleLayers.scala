package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier
import org.scalajs.dom
import scala.scalajs.js

/** The per-element stack of `Style` contributions.
  *
  * Every lui style modifier — a static `Style`, a `ThemedStyle`, `signal.styled`, `themed`
  * — claims its own layer here instead of writing the whole `style` attribute. Layers are
  * indexed in the order the modifiers were applied and flushed together, so two styles on
  * one element compose rather than race, and CSS last-wins still resolves a genuine
  * conflict in favour of the later modifier.
  *
  * Only the properties lui itself declared are ever removed, so an element can still carry
  * Laminar's own style setters alongside a lui `Style`.
  *
  * The stack lives on the DOM node under a private key, so it is found again by every
  * modifier applied to the same element and collected with the node. */
private[style] final class StyleLayers(node: dom.HTMLElement) {

  private val layers: js.Array[Vector[Decl]] = js.Array()
  private var owned: Vector[String] = Vector.empty

  def claim(): Int = {
    layers.push(Vector.empty)
    layers.length - 1
  }

  def update(layer: Int, decls: Vector[Decl]): Unit = {
    layers(layer) = decls
    val merged = layers.toVector.flatten
    val declared = merged.iterator.map(_.prop).toVector.distinct
    owned.iterator.filterNot(declared.contains).foreach(node.style.removeProperty)
    // Clear before setting, so a value the CSSOM rejects leaves the property
    // absent rather than silently keeping whatever was written last.
    merged.foreach { d =>
      node.style.removeProperty(d.prop)
      node.style.setProperty(d.prop, d.value)
    }
    owned = declared
  }
}

private[style] object StyleLayers {

  private val key = "__luiStyleLayers"

  private def of(el: HtmlElement): StyleLayers = {
    val holder = el.ref.asInstanceOf[js.Dynamic]
    val existing = holder.selectDynamic(key)
    if (js.isUndefined(existing)) {
      val fresh = new StyleLayers(el.ref)
      holder.updateDynamic(key)(fresh.asInstanceOf[js.Any])
      fresh
    } else existing.asInstanceOf[StyleLayers]
  }

  /** Modifier that contributes a fixed set of declarations as one layer. */
  def static(decls: Vector[Decl]): Modifier[HtmlElement] = new Modifier[HtmlElement] {
    override def apply(el: HtmlElement): Unit = {
      val stack = of(el)
      stack.update(stack.claim(), decls)
    }
  }

  /** Modifier that contributes one layer kept in step with `styles`. The layer is claimed
    * when the modifier is applied, so its position in the stack follows source order even
    * though its content arrives on subscription. */
  def dynamic(styles: Signal[Style]): Modifier[HtmlElement] = new Modifier[HtmlElement] {
    override def apply(el: HtmlElement): Unit = {
      val stack = of(el)
      val layer = stack.claim()
      el.amend(styles.map(_.decls) --> Observer[Vector[Decl]](stack.update(layer, _)))
    }
  }
}
