package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}
import com.raquo.laminar.modifiers.Modifier

/** A single CSS declaration. `prop` is the CSS property name (e.g. "background-color");
  * `value` is the serialized CSS value (e.g. "rgb(13, 148, 136)"). */
final case class Decl(prop: String, value: String)

/** A composable set of CSS declarations. Acts as a Laminar `Modifier[HtmlElement]` — you can
  * drop it straight into a tag: `div(stack.col(spacing.lg), child1, child2)`. */
final class Style(val decls: Vector[Decl]) extends Modifier[HtmlElement] {

  def toCss: String = {
    val sb = new StringBuilder()
    var first = true
    val it = decls.iterator
    while (it.hasNext) {
      val d = it.next()
      if (!first) sb.append("; ")
      sb.append(d.prop)
      sb.append(": ")
      sb.append(d.value)
      first = false
    }
    sb.toString
  }

  def ++(other: Style): Style = new Style(decls ++ other.decls)
  def :+(d: Decl): Style = new Style(decls.appended(d))

  override def apply(el: HtmlElement): Unit = {
    if (decls.nonEmpty) {
      val _ = (styleAttr := toCss).apply(el)
    }
  }
}

object Style {
  val empty: Style = new Style(Vector.empty)

  def apply(decls: Decl*): Style = new Style(decls.toVector)

  /** Tuple-shaped constructor for property → value pairs. Prefer the typed builders in `css`. */
  def of(pairs: (String, String)*): Style =
    new Style(pairs.iterator.map(p => Decl(p._1, p._2)).toVector)
}

/** Typed declaration builders. Each function takes typed values and returns a one-declaration
  * `Style`. Compose with `++`. */
object css {
  private inline def one(prop: String, value: String): Style = Style(Decl(prop, value))

  // Color
  def background(c: Color): Style = one("background", c.toCss)
  def color(c: Color): Style = one("color", c.toCss)
  def borderColor(c: Color): Style = one("border-color", c.toCss)
  def boxShadow(v: String): Style = one("box-shadow", v)

  // Lengths
  def width(l: Length): Style = one("width", l.toCss)
  def height(l: Length): Style = one("height", l.toCss)
  def minWidth(l: Length): Style = one("min-width", l.toCss)
  def maxWidth(l: Length): Style = one("max-width", l.toCss)
  def minHeight(l: Length): Style = one("min-height", l.toCss)
  def maxHeight(l: Length): Style = one("max-height", l.toCss)
  def padding(a: Length): Style = one("padding", a.toCss)
  def padding(v: Length, h: Length): Style = one("padding", s"${v.toCss} ${h.toCss}")
  def margin(a: Length): Style = one("margin", a.toCss)
  def margin(v: Length, h: Length): Style = one("margin", s"${v.toCss} ${h.toCss}")
  def margin(t: Length, r: Length, b: Length, l: Length): Style =
    one("margin", s"${t.toCss} ${r.toCss} ${b.toCss} ${l.toCss}")
  def gap(l: Length): Style = one("gap", l.toCss)
  def gap(row: Length, col: Length): Style = one("gap", s"${row.toCss} ${col.toCss}")
  def rowGap(l: Length): Style = one("row-gap", l.toCss)
  def columnGap(l: Length): Style = one("column-gap", l.toCss)

  def gridTemplateColumns(spec: String): Style = one("grid-template-columns", spec)
  def gridTemplateRows(spec: String): Style = one("grid-template-rows", spec)
  def gridTemplateAreas(spec: String): Style = one("grid-template-areas", spec)
  def gridColumn(spec: String): Style = one("grid-column", spec)
  def gridRow(spec: String): Style = one("grid-row", spec)
  def gridArea(spec: String): Style = one("grid-area", spec)
  def gridAutoFlow(v: String): Style = one("grid-auto-flow", v)
  def gridAutoColumns(spec: String): Style = one("grid-auto-columns", spec)
  def gridAutoRows(spec: String): Style = one("grid-auto-rows", spec)

  // Borders
  def border(w: Length, st: BorderStyle, c: Color): Style =
    one("border", s"${w.toCss} ${st.toCss} ${c.toCss}")
  def borderRadius(r: Length): Style = one("border-radius", r.toCss)
  def borderTop(w: Length, st: BorderStyle, c: Color): Style =
    one("border-top", s"${w.toCss} ${st.toCss} ${c.toCss}")
  def borderRight(w: Length, st: BorderStyle, c: Color): Style =
    one("border-right", s"${w.toCss} ${st.toCss} ${c.toCss}")
  def borderBottom(w: Length, st: BorderStyle, c: Color): Style =
    one("border-bottom", s"${w.toCss} ${st.toCss} ${c.toCss}")
  def borderLeft(w: Length, st: BorderStyle, c: Color): Style =
    one("border-left", s"${w.toCss} ${st.toCss} ${c.toCss}")

  // Typography
  def fontSize(l: Length): Style = one("font-size", l.toCss)
  def fontWeight(w: FontWeight): Style = one("font-weight", w.toCss)
  def fontStyle(v: String): Style = one("font-style", v)
  def letterSpacing(l: Length): Style = one("letter-spacing", l.toCss)
  def textTransform(v: String): Style = one("text-transform", v)
  def textAlign(v: TextAlign): Style = one("text-align", v.toCss)
  def textOverflow(v: String): Style = one("text-overflow", v)
  def lineHeight(v: Double): Style = one("line-height", v.toString)
  def whiteSpace(v: String): Style = one("white-space", v)
  def userSelect(v: String): Style = one("user-select", v)

  // Flex / layout
  def display(d: Display): Style = one("display", d.toCss)
  def flexDirection(v: String): Style = one("flex-direction", v)
  def alignItems(v: String): Style = one("align-items", v)
  def justifyContent(v: String): Style = one("justify-content", v)
  def flexWrap(v: String): Style = one("flex-wrap", v)
  def flexShrink(v: Int): Style = one("flex-shrink", v.toString)
  def flexGrow(v: Int): Style = one("flex-grow", v.toString)
  def flex(grow: Int, shrink: Int, basis: Length): Style =
    one("flex", s"$grow $shrink ${basis.toCss}")

  // Position
  def position(v: String): Style = one("position", v)
  def top(l: Length): Style = one("top", l.toCss)
  def right(l: Length): Style = one("right", l.toCss)
  def bottom(l: Length): Style = one("bottom", l.toCss)
  def left(l: Length): Style = one("left", l.toCss)
  def zIndex(v: Int): Style = one("z-index", v.toString)

  // Transitions / transforms
  def transition(prop: String, ms: Int): Style = one("transition", s"$prop ${ms}ms ease")
  def transform(v: String): Style = one("transform", v)

  // Misc
  def cursor(v: String): Style = one("cursor", v)
  def opacity(v: Double): Style = one("opacity", v.toString)
  def overflow(v: String): Style = one("overflow", v)
  def overflowY(v: String): Style = one("overflow-y", v)
  def overflowX(v: String): Style = one("overflow-x", v)
  def pointerEvents(v: String): Style = one("pointer-events", v)

  // Compound presets — small, recurring multi-decl combinations that
  // come up across most apps. Each composes typed builders so the
  // call site still reads like real CSS but stays short.

  /** Single-line truncation with ellipsis. Pairs with a parent that
    * has a constrained width and `min-width: 0` (e.g. `stack.fill`
    * or an explicit `css.minWidth(Length.zero)`). */
  val ellipsis: Style =
    overflow("hidden") ++ textOverflow("ellipsis") ++ whiteSpace("nowrap")

  /** Disable text selection (logos, avatars, toggle pills, button
    * glyphs — anywhere a user dragging across the element shouldn't
    * highlight text). */
  val selectNone: Style = userSelect("none")

  /** Italic. */
  val italic: Style = fontStyle("italic")

  /** Disable pointer events. Use on decorative overlays (toast
    * bubbles, sticky tooltips, badge dots) that mustn't intercept
    * clicks/hover on the layer beneath. */
  val pointerNone: Style = pointerEvents("none")

  /** Escape hatch for properties not yet covered. Add a typed builder above before reaching for this. */
  def raw(prop: String, value: String): Style = one(prop, value)
}
