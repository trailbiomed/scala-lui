package lui.style

opaque type Length = String

object Length {
  val zero: Length = "0"
  val auto: Length = "auto"

  /** Construct a Length from a literal CSS string. */
  def raw(css: String): Length = css

  // Factory methods — unambiguous regardless of what else is in scope.
  def px(n: Int): Length = raw(s"${n}px")
  def px(d: Double): Length = raw(s"${d}px")
  def pct(n: Int): Length = raw(s"${n}%")
  def rem(d: Double): Length = raw(s"${d}rem")
  def em(d: Double): Length = raw(s"${d}em")

  // Extensions for terse internal use (tokens.scala). Hidden inside the object so they
  // don't conflict with Laminar's identically-named extensions on Int/Double when both
  // packages are wildcard-imported in component files.
  extension (i: Int) {
    def lpx: Length = raw(s"${i}px")
    def lpct: Length = raw(s"${i}%")
  }
  extension (d: Double) {
    def lpx: Length = raw(s"${d}px")
    def lrem: Length = raw(s"${d}rem")
    def lem: Length = raw(s"${d}em")
  }
}

/** `.toCss` exposes the underlying CSS string. Implemented via universal `toString`, which
  * at runtime returns the backing String since opaque types are erased to their underlying. */
extension (l: Length) {
  def toCss: String = l.toString
}
