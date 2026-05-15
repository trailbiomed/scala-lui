package lui.style

import lui.style.Length.{lpx, lem as _}

/** Color palette */
object palette {
  // Brand (teal)
  val teal50: Color = Color.hex("#f0fdfa")
  val teal100: Color = Color.hex("#ccfbf1")
  val teal200: Color = Color.hex("#99f6e4")
  val teal400: Color = Color.hex("#2dd4bf")
  val teal500: Color = Color.hex("#14b8a6")
  val teal600: Color = Color.hex("#0d9488")
  val teal700: Color = Color.hex("#0f766e")
  val teal900: Color = Color.hex("#134e4a")

  // Neutrals (slate — kept for data-encoding / status use cases
  // that want a cool blue-gray; the Theme uses the warmer `neutral`
  // scale below for chrome).
  val white: Color = Color.hex("#ffffff")
  val slate50: Color = Color.hex("#f8fafc")
  val slate100: Color = Color.hex("#f1f5f9")
  val slate200: Color = Color.hex("#e2e8f0")
  val slate300: Color = Color.hex("#cbd5e1")
  val slate400: Color = Color.hex("#94a3b8")
  val slate500: Color = Color.hex("#64748b")
  val slate600: Color = Color.hex("#475569")
  val slate700: Color = Color.hex("#334155")
  val slate800: Color = Color.hex("#1e293b")
  val slate900: Color = Color.hex("#0f172a")

  // Neutrals (zinc/gray — Chakra v3's default `gray` palette,
  // visually neutral with no blue undertone). The Theme uses these
  // for surface / border / text by default.
  val neutral50: Color  = Color.hex("#fafafa")
  val neutral100: Color = Color.hex("#f4f4f5")
  val neutral200: Color = Color.hex("#e4e4e7")
  val neutral300: Color = Color.hex("#d4d4d8")
  val neutral400: Color = Color.hex("#a1a1aa")
  val neutral500: Color = Color.hex("#71717a")
  val neutral600: Color = Color.hex("#52525b")
  val neutral700: Color = Color.hex("#3f3f46")
  val neutral800: Color = Color.hex("#27272a")
  val neutral900: Color = Color.hex("#18181b")
  val neutral950: Color = Color.hex("#111111")

  // Semantic — emerald (success)
  val emerald50: Color = Color.hex("#ecfdf5")
  val emerald300: Color = Color.hex("#6ee7b7")
  val emerald600: Color = Color.hex("#059669")
  val emerald700: Color = Color.hex("#047857")

  // Semantic — red (danger / batch warning)
  val red50: Color = Color.hex("#fef2f2")
  val red300: Color = Color.hex("#fca5a5")
  val red600: Color = Color.hex("#dc2626")
  val red800: Color = Color.hex("#991b1b")

  // Semantic — blue (running / info)
  val blue50: Color = Color.hex("#eff6ff")
  val blue300: Color = Color.hex("#93c5fd")
  val blue600: Color = Color.hex("#2563eb")

  // Semantic — amber (warning / sweep)
  val amber50: Color = Color.hex("#fffbeb")
  val amber100: Color = Color.hex("#fef3c7")
  val amber300: Color = Color.hex("#fcd34d")
  val amber700: Color = Color.hex("#b45309")
  val amber800: Color = Color.hex("#92400e")

  // Black with alpha (modal backdrop, shadows etc.)
  val backdrop: Color = Color(15, 23, 42, 0.4)
}

/** Border-radius tokens. */
object radius {
  val sm: Length = 6.lpx
  val md: Length = 8.lpx
  val lg: Length = 12.lpx
  val xl: Length = 16.lpx
  val pill: Length = 9999.lpx
}

/** Spacing tokens (padding, gap, margin). */
object spacing {
  val xs: Length = 4.lpx
  val sm: Length = 6.lpx
  val md: Length = 8.lpx
  val lg: Length = 12.lpx
  val xl: Length = 16.lpx
  val xxl: Length = 24.lpx
  val xxxl: Length = 32.lpx
}

/** Type-size tokens. */
object fontSizes {
  val xs: Length = 10.lpx
  val sm: Length = 11.lpx
  val md: Length = 12.lpx
  val lg: Length = 13.lpx
  val xl: Length = 14.lpx
  val xxl: Length = 16.lpx
  val xxxl: Length = 18.lpx
  val display: Length = 20.lpx
}

/** Responsive breakpoints. */
object breakpoints {
  val sm: Length = 640.lpx
  val md: Length = 768.lpx
  val lg: Length = 1024.lpx
  val xl: Length = 1280.lpx
}
