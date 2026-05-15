package lui.style

final case class Color(r: Int, g: Int, b: Int, a: Double = 1.0) {
  def toCss: String = {
    if (a >= 1.0) s"rgb($r, $g, $b)"
    else s"rgba($r, $g, $b, ${f"$a%.3f"})"
  }
  def alpha(value: Double): Color = copy(a = value)
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
