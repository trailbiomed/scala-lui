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
    bg = palette.neutral50,
    surface = palette.white,
    surfaceDim = palette.neutral50,
    backdrop = palette.backdrop,
    border = palette.neutral200,
    borderActive = palette.teal600,
    text = palette.neutral900,
    textMuted = palette.neutral500,
    textSubtle = palette.neutral400,
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

  val monokai: Theme = Theme(
    name = "monokai",
    isDark = true,
    bg = Color.hex("#1e1f1c"),
    surface = Color.hex("#272822"),
    surfaceDim = Color.hex("#1a1b17"),
    backdrop = Color(0, 0, 0, 0.6),
    border = Color.hex("#3e3d32"),
    borderActive = Color.hex("#66d9ef"),
    text = Color.hex("#f8f8f2"),
    textMuted = Color.hex("#a59f85"),
    textSubtle = Color.hex("#75715e"),
    brand = Color.hex("#66d9ef"),
    brandSoft = Color(102, 217, 239, 0.18),
    brandHover = Color.hex("#a6e7f4"),
    onBrand = Color.hex("#272822"),
    success = Color.hex("#a6e22e"),
    successSoft = Color(166, 226, 46, 0.18),
    successBorder = Color.hex("#769b1f"),
    warning = Color.hex("#fd971f"),
    warningSoft = Color(253, 151, 31, 0.18),
    warningBorder = Color.hex("#b8741a"),
    danger = Color.hex("#f92672"),
    dangerSoft = Color(249, 38, 114, 0.18),
    dangerBorder = Color.hex("#b3185a"),
    info = Color.hex("#ae81ff"),
    infoSoft = Color(174, 129, 255, 0.18),
    infoBorder = Color.hex("#7b5cb5")
  )

  val dark: Theme = Theme(
    name = "dark",
    isDark = true,
    bg = Color.hex("#0c0c0e"),
    surface = Color.hex("#18181b"),
    surfaceDim = Color.hex("#111114"),
    backdrop = Color(0, 0, 0, 0.6),
    border = palette.neutral700,
    borderActive = palette.teal400,
    text = palette.neutral100,
    textMuted = palette.neutral400,
    textSubtle = palette.neutral500,
    brand = palette.teal400,
    brandSoft = Color(20, 184, 166, 0.18),
    brandHover = palette.teal500,
    onBrand = palette.neutral900,
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
