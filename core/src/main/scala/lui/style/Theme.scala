package lui.style

import com.raquo.laminar.api.L.{Mod as _, *}

/** Semantic color tokens. Components reference these (`t.surface`, `t.text`, …) instead of
  * raw palette stops; switching themes swaps every component's colors at once. */
final case class Theme(
    name: String,
    isDark: Boolean,
    // Surfaces
    bg: Color,
    surface: Color,
    surfaceDim: Color,
    backdrop: Color,
    // Borders
    border: Color,
    borderActive: Color,
    // Text
    text: Color,
    textMuted: Color,
    textSubtle: Color,
    // Brand
    brand: Color,
    brandSoft: Color,
    brandHover: Color,
    onBrand: Color, // text on brand surface
    // Status (text / soft bg / border triples)
    success: Color,
    successSoft: Color,
    successBorder: Color,
    warning: Color,
    warningSoft: Color,
    warningBorder: Color,
    danger: Color,
    dangerSoft: Color,
    dangerBorder: Color,
    info: Color,
    infoSoft: Color,
    infoBorder: Color
)

object Theme {

  val light: Theme = Theme(
    name = "light",
    isDark = false,
    bg = palette.slate50,
    surface = palette.white,
    surfaceDim = palette.slate50,
    backdrop = palette.backdrop,
    border = palette.slate200,
    borderActive = palette.teal600,
    text = palette.slate900,
    textMuted = palette.slate500,
    textSubtle = palette.slate400,
    brand = palette.teal600,
    brandSoft = palette.teal50,
    brandHover = palette.teal700,
    onBrand = palette.white,
    success = palette.emerald600,
    successSoft = palette.emerald50,
    successBorder = palette.emerald300,
    warning = palette.amber800,
    warningSoft = palette.amber100,
    warningBorder = palette.amber300,
    danger = palette.red600,
    dangerSoft = palette.red50,
    dangerBorder = palette.red300,
    info = palette.blue600,
    infoSoft = palette.blue50,
    infoBorder = palette.blue300
  )

  val dark: Theme = Theme(
    name = "dark",
    isDark = true,
    bg = Color.hex("#0b1220"),
    surface = Color.hex("#152033"),
    surfaceDim = Color.hex("#101827"),
    backdrop = Color(0, 0, 0, 0.6),
    border = palette.slate700,
    borderActive = palette.teal400,
    text = palette.slate100,
    textMuted = palette.slate400,
    textSubtle = palette.slate500,
    brand = palette.teal400,
    brandSoft = Color(20, 184, 166, 0.18),
    brandHover = palette.teal500,
    onBrand = palette.slate900,
    success = palette.emerald300,
    successSoft = Color(6, 95, 70, 0.30),
    successBorder = palette.emerald700,
    warning = palette.amber300,
    warningSoft = Color(120, 53, 15, 0.30),
    warningBorder = palette.amber700,
    danger = palette.red300,
    dangerSoft = Color(127, 29, 29, 0.30),
    dangerBorder = palette.red800,
    info = palette.blue300,
    infoSoft = Color(30, 58, 138, 0.30),
    infoBorder = palette.blue600
  )

  /** Global reactive theme. Components subscribe via `Theme.signal`. */
  val current: Var[Theme] = Var(light)
  val signal: Signal[Theme] = current.signal

  def setLight(): Unit = current.set(light)
  def setDark(): Unit = current.set(dark)
  def toggle(): Unit = if (current.now().isDark) setLight() else setDark()
}
