package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier
import org.scalajs.dom
import scala.scalajs.js

/** The stack of `Style` contributions on one element. Each lui style modifier claims a
  * layer instead of writing the whole `style` attribute, so styles on the same element
  * compose; later layers win the properties they share. Only properties lui declared are
  * removed, leaving Laminar's own style setters untouched. */
private[style] final class StyleLayers(node: dom.HTMLElement) {

  private val layers: js.Array[Vector[Decl]] = js.Array()
  private var luiDeclaredProps: Vector[String] = Vector.empty

  def claim(): Int = {
    layers.push(Vector.empty)
    layers.length - 1
  }

  def update(layer: Int, decls: Vector[Decl]): Unit = {
    layers(layer) = decls
    val lastWins = layers.toVector.flatten
    val props = lastWins.iterator.map(_.prop).toVector.distinct
    val released = luiDeclaredProps.iterator.filterNot(props.contains)
    released.foreach(node.style.removeProperty)
    lastWins.foreach(replaceProperty)
    luiDeclaredProps = props
  }

  /** Removes before setting, so a value the CSSOM rejects leaves no stale predecessor. */
  private def replaceProperty(d: Decl): Unit = {
    val _ = node.style.removeProperty(d.prop)
    node.style.setProperty(d.prop, d.value)
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

  /** Modifier contributing one layer kept in step with `styles`. The layer is claimed when
    * the modifier is applied, so its position follows source order rather than
    * subscription order. */
  def dynamic(styles: Signal[Style]): Modifier[HtmlElement] = new Modifier[HtmlElement] {
    override def apply(el: HtmlElement): Unit = {
      val stack = of(el)
      val layer = stack.claim()
      el.amend(styles.map(_.decls) --> Observer[Vector[Decl]](stack.update(layer, _)))
    }
  }
}
