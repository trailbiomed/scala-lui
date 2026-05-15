package lui.style

/** Common layout style compositions. Pure layout — no theme dependency. */
object stack {

  /** Vertical column. */
  def col(gap: Length = Length.zero): Style =
    css.display(Display.Flex) ++ css.flexDirection("column") ++ css.gap(gap)

  /** Horizontal row, items centered vertically. */
  def row(gap: Length = Length.zero): Style =
    css.display(Display.Flex) ++ css.alignItems("center") ++ css.gap(gap)

  /** Horizontal row, items pushed to opposite ends, centered vertically. */
  def between(gap: Length = Length.zero): Style =
    row(gap) ++ css.justifyContent("space-between")

  /** Center children on both axes. */
  val centerAll: Style =
    css.display(Display.Flex) ++ css.alignItems("center") ++ css.justifyContent("center")

  /** Allow flex children to wrap onto multiple lines. */
  val wrap: Style = css.flexWrap("wrap")

  /** Prevent a flex child from shrinking. */
  val noShrink: Style = css.flexShrink(0)

  /** Take all remaining flex space along the main axis. */
  val grow: Style = css.flexGrow(1)
}

/** Typography presets — themed text styles. Each value is a `ThemedStyle`, so it doubles as
  * a Laminar `Modifier[HtmlElement]`: `span(typo.h1, "title")`. Compose with `++`. */
object typo {

  val eyebrow: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xs) ++
      css.fontWeight(FontWeight.Bold) ++
      css.color(t.textSubtle) ++
      css.textTransform("uppercase") ++
      css.letterSpacing(Length.em(0.05))
  }

  val h1: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.display) ++
      css.fontWeight(FontWeight.SemiBold) ++
      css.color(t.text)
  }

  val h2: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xxxl) ++
      css.fontWeight(FontWeight.SemiBold) ++
      css.color(t.text)
  }

  val label: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.lg) ++
      css.fontWeight(FontWeight.Medium) ++
      css.color(t.text)
  }

  val body: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.lg) ++
      css.color(t.text)
  }

  val muted: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.md) ++
      css.color(t.textMuted)
  }

  val hint: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.sm) ++
      css.color(t.textSubtle)
  }
}

/** Themed background surfaces — same `ThemedStyle` model. */
object surface {

  val card: ThemedStyle = ThemedStyle { t =>
    css.background(t.surface) ++
      css.borderRadius(radius.xl) ++
      css.border(Length.px(1.5), BorderStyle.Solid, t.border)
  }

  val dim: ThemedStyle = ThemedStyle { t =>
    css.background(t.surfaceDim) ++
      css.border(Length.px(1), BorderStyle.Solid, t.border) ++
      css.borderRadius(radius.md)
  }
}
