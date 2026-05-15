package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object IconPages {

  def icon(): HtmlElement = PageTemplate(
    title = "Icon",
    summary = "Wraps a glyph or SVG with a fixed size. Uses currentColor so the parent can recolor."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Icon(size)",
        """Icon(size = Length.px(12))("★")
          |Icon(size = Length.px(16))("★")
          |Icon(size = Length.px(20))("★")
          |Icon(size = Length.px(28))("★")""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Icon(size = Length.px(12))("★"),
          Icon(size = Length.px(16))("★"),
          Icon(size = Length.px(20))("★"),
          Icon(size = Length.px(28))("★")
        )
      )
    ),
    PageTemplate.section("Colors")(
      PageTemplate.codedDemo(
        "Icon(color)",
        """Icon(size = Length.px(20), color = Some(palette.teal600))("●")
          |Icon(size = Length.px(20), color = Some(palette.red600))("●")
          |Icon(size = Length.px(20), color = Some(palette.amber700))("●")""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Icon(size = Length.px(20), color = Some(palette.teal600))("●"),
          Icon(size = Length.px(20), color = Some(palette.red600))("●"),
          Icon(size = Length.px(20), color = Some(palette.amber700))("●"),
          Icon(size = Length.px(20), color = Some(palette.slate500))("●")
        )
      )
    ),
    PageTemplate.propsTable(
      ("size",  "Length",         "Width, height, and font-size of the icon. Default 16px."),
      ("color", "Option[Color]",  "Optional explicit color. If None, inherits from parent (currentColor)."),
      ("glyph", "Modifier[HtmlElement]*", "Glyph contents (string, SVG, etc).")
    )
  )

  def checkmark(): HtmlElement = PageTemplate(
    title = "Checkmark",
    summary = "Bare checkmark glyph. Used inside Checkbox; exposed for custom contexts."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Checkmark(size)",
        """Checkmark(Length.px(11))
          |Checkmark(Length.px(16))
          |Checkmark(Length.px(24))""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          themed(t => css.color(t.brand)),
          Checkmark(Length.px(11)),
          Checkmark(Length.px(16)),
          Checkmark(Length.px(24))
        )
      )
    ),
    PageTemplate.behavior(
      "Color comes from the parent (currentColor). Set css.color(...) on the wrapper to tint."
    ),
    PageTemplate.propsTable(
      ("size", "Length", "Font-size of the glyph. Default 12px.")
    )
  )

  def radiomark(): HtmlElement = PageTemplate(
    title = "Radiomark",
    summary = "Filled brand-color dot used inside RadioGroup."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Radiomark(size)",
        """Radiomark(Length.px(6))
          |Radiomark(Length.px(10))
          |Radiomark(Length.px(14))""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          Radiomark(Length.px(6)),
          Radiomark(Length.px(10)),
          Radiomark(Length.px(14))
        )
      )
    ),
    PageTemplate.propsTable(
      ("size", "Length", "Diameter of the dot. Default 8px.")
    )
  )

  def colorSwatch(): HtmlElement = PageTemplate(
    title = "ColorSwatch",
    summary = "Filled tile previewing a Color. Useful in palettes and color pickers."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "ColorSwatch(color, size)",
        """ColorSwatch(palette.teal600, Length.px(20))
          |ColorSwatch(palette.teal600, Length.px(40))
          |ColorSwatch(palette.teal600, Length.px(56))""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          ColorSwatch(palette.teal600, Length.px(20)),
          ColorSwatch(palette.teal600, Length.px(28)),
          ColorSwatch(palette.teal600, Length.px(40)),
          ColorSwatch(palette.teal600, Length.px(56))
        )
      )
    ),
    PageTemplate.section("Square corners")(
      PageTemplate.codedDemo(
        "rounded = false",
        """ColorSwatch(palette.red600,     Length.px(40), rounded = false)
          |ColorSwatch(palette.amber700,   Length.px(40), rounded = false)
          |ColorSwatch(palette.emerald600, Length.px(40), rounded = false)""".stripMargin
      )(
        div(stack.row(spacing.md),
          ColorSwatch(palette.red600,    Length.px(40), rounded = false),
          ColorSwatch(palette.amber700,  Length.px(40), rounded = false),
          ColorSwatch(palette.emerald600,Length.px(40), rounded = false),
          ColorSwatch(palette.blue600,   Length.px(40), rounded = false)
        )
      )
    ),
    PageTemplate.propsTable(
      ("c",       "Color",   "The color to render."),
      ("size",    "Length",  "Width and height. Default 20px."),
      ("rounded", "Boolean", "If true (default), uses radius.sm corners.")
    )
  )
}
