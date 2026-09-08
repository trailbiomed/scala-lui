package lui.style

final case class Color(r: Int, g: Int, b: Int, a: Double = 1.0) {
  def toCss: String = {
    if (a >= 1.0) s"rgb($r, $g, $b)"
    else s"rgba($r, $g, $b, ${f"$a%.3f"})"
  }
  def alpha(value: Double): Color = copy(a = value)

  /** Flatten this color onto an opaque `under`, the way the browser composites it. Needed
    * before any luminance calculation, since the soft status tokens in the dark themes are
    * translucent. */
  def over(under: Color): Color = {
    if (a >= 1.0) copy(a = 1.0)
    else {
      def mix(f: Int, b: Int): Int = Math.round(f * a + b * (1.0 - a)).toInt
      Color(mix(r, under.r), mix(g, under.g), mix(b, under.b))
    }
  }
}

object Color {
  val transparent: Color = Color(0, 0, 0, 0.0)

  /** Parse a `#rrggbb` literal into a fully-opaque Color. */
  def hex(s: String): Color = {
    val h = s.stripPrefix("#")
    require(h.length == 6, s"expected #rrggbb, got '$s'")
    Color(
      Integer.parseInt(h.substring(0, 2), 16),
      Integer.parseInt(h.substring(2, 4), 16),
      Integer.parseInt(h.substring(4, 6), 16)
    )
  }
}
