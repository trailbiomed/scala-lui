package lui.style

/** WCAG 2.1 contrast arithmetic over [[Color]], plus an audit of the pairings lui's own
  * components draw. Since lui owns both halves of every such pairing, it is also the only
  * place a failing one can be fixed. */
object Contrast {

  /** Minimum ratio for body text under WCAG 2.1 AA (1.4.3). */
  val aa: Double = 4.5

  /** Minimum ratio for text at 18.66px bold or 24px regular (WCAG 2.1 AA 1.4.3), and for
    * a control's visual boundary (1.4.11). */
  val aaLarge: Double = 3.0

  /** Relative luminance per WCAG 2.1, flattening a translucent `c` onto white. */
  def luminance(c: Color): Double = {
    val flat = c.over(palette.white)
    def channel(v: Int): Double = {
      val s = v / 255.0
      if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
    }
    0.2126 * channel(flat.r) + 0.7152 * channel(flat.g) + 0.0722 * channel(flat.b)
  }

  /** Contrast ratio of `fg` drawn on `bg`, from 1.0 (identical) to 21.0 (black on white).
    * Composites a translucent `fg` onto `bg`; flatten a translucent `bg` yourself with
    * [[Color.over]] first. */
  def ratio(fg: Color, bg: Color): Double = {
    val a = luminance(fg.over(bg))
    val b = luminance(bg)
    val hi = Math.max(a, b)
    val lo = Math.min(a, b)
    (hi + 0.05) / (lo + 0.05)
  }

  /** One measured pairing, against the threshold that applies to it. */
  final case class Pairing(
      theme: String,
      foreground: String,
      background: String,
      measured: Double,
      required: Double
  ) {
    def passes: Boolean = measured >= required
    def describe: String =
      f"$theme: $foreground on $background is $measured%.2f:1, needs $required%.1f:1"
  }

  /** Every text pairing lui's components draw, measured against `t`. `textSubtle` is
    * absent by design: it is the decoration step of the text scale, never a carrier of
    * text a reader needs. */
  def audit(t: Theme): Seq[Pairing] = {
    val grounds = Seq("bg" -> t.bg, "surface" -> t.surface, "surfaceDim" -> t.surfaceDim)
    def onGrounds(fgName: String, fg: Color): Seq[Pairing] =
      grounds.map { case (bgName, bg) =>
        Pairing(t.name, fgName, bgName, ratio(fg, bg), aa)
      }
    def onSoft(fgName: String, fg: Color, softName: String, soft: Color): Pairing =
      Pairing(t.name, fgName, softName, ratio(fg, soft.over(t.surface)), aa)

    onGrounds("text", t.text) ++
      onGrounds("textMuted", t.textMuted) ++
      onGrounds("brand", t.brand) ++
      onGrounds("success", t.success) ++
      onGrounds("warning", t.warning) ++
      onGrounds("danger", t.danger) ++
      onGrounds("info", t.info) ++
      Seq(
        Pairing(t.name, "onBrand", "brand", ratio(t.onBrand, t.brand), aa),
        Pairing(t.name, "onBrand", "brandHover", ratio(t.onBrand, t.brandHover), aa),
        onSoft("brand", t.brand, "brandSoft", t.brandSoft),
        onSoft("success", t.success, "successSoft", t.successSoft),
        onSoft("warning", t.warning, "warningSoft", t.warningSoft),
        onSoft("danger", t.danger, "dangerSoft", t.dangerSoft),
        onSoft("info", t.info, "infoSoft", t.infoSoft)
      )
  }

  /** The pairings of `t` that fall short. Empty for every theme lui ships. */
  def failures(t: Theme): Seq[Pairing] = audit(t).filterNot(_.passes)
}
