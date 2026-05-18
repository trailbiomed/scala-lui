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

  /** Fill the remaining flex space **and** unlock the min-content
    * floor on both axes. Equivalent to
    * `flex: 1 1 0; min-width: 0; min-height: 0`.
    *
    * Use on a flex child that needs to scroll its own overflow, or
    * that wraps an ellipsis text node. Without the `min-*: 0`
    * unlock, the child refuses to shrink below its content's
    * intrinsic width/height and breaks scroll / ellipsis. */
  val fill: Style =
    css.flex(1, 1, Length.zero) ++ css.minWidth(Length.zero) ++ css.minHeight(Length.zero)
}

/** Text-color override channel. Typography presets resolve their `color` through this
  * channel, so a parent that calls `fg.override(c)` recolors every typo descendant in one
  * shot — used by `Navbar`'s `Brand` variant to flip text to `onBrand` without per-element
  * styling. Implemented as a CSS custom property; the name is private to this object. */
object fg {

  private val varName = "--lui-fg"

  /** Use as the `color` decl in a themed preset. Resolves to the parent-supplied override
    * if any ancestor called `fg.set(...)`, otherwise to `fallback`. */
  def color(fallback: Color): Style =
    css.raw("color", s"var($varName, ${fallback.toCss})")

  /** Set on an ancestor element to recolor every `fg.color(...)` descendant. */
  def set(c: Color): Style =
    css.raw(varName, c.toCss)
}

/** Typography presets — themed text styles. Each value is a `ThemedStyle`, so it doubles as
  * a Laminar `Modifier[HtmlElement]`: `span(typo.h1, "title")`. Compose with `++`. Text
  * color goes through `fg` so a parent can override every typo descendant in one shot. */
object typo {

  val eyebrow: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xs) ++
      css.fontWeight(FontWeight.Bold) ++
      fg.color(t.textSubtle) ++
      css.textTransform("uppercase") ++
      css.letterSpacing(Length.em(0.05))
  }

  val h1: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.display) ++
      css.fontWeight(FontWeight.SemiBold) ++
      fg.color(t.text)
  }

  val h2: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xxxl) ++
      css.fontWeight(FontWeight.SemiBold) ++
      fg.color(t.text)
  }

  val label: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xl) ++
      css.fontWeight(FontWeight.Medium) ++
      fg.color(t.text)
  }

  val body: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.xl) ++
      fg.color(t.text)
  }

  val muted: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.md) ++
      fg.color(t.textMuted)
  }

  val hint: ThemedStyle = ThemedStyle { t =>
    css.fontSize(fontSizes.md) ++
      fg.color(t.textSubtle)
  }
}

/** Themed background surfaces — same `ThemedStyle` model. */
object surface {

  val card: ThemedStyle = ThemedStyle { t =>
    css.background(t.surface) ++
      css.borderRadius(radius.xl) ++
      css.border(Length.px(1), BorderStyle.Solid, t.border)
  }

  val dim: ThemedStyle = ThemedStyle { t =>
    css.background(t.surfaceDim) ++
      css.border(Length.px(1), BorderStyle.Solid, t.border) ++
      css.borderRadius(radius.md)
  }
}
