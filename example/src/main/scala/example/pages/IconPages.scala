package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object IconPages {

  /** The curated Lucide glyph set shipped as `lui.components.icons.*`. */
  private val lucideEntries: Seq[(String, SvgElement)] = Seq(
    "chevronLeft"    -> icons.chevronLeft,
    "chevronRight"   -> icons.chevronRight,
    "chevronUp"      -> icons.chevronUp,
    "chevronDown"    -> icons.chevronDown,
    "arrowLeft"      -> icons.arrowLeft,
    "arrowRight"     -> icons.arrowRight,
    "arrowUp"        -> icons.arrowUp,
    "arrowDown"      -> icons.arrowDown,
    "externalLink"   -> icons.externalLink,
    "x"              -> icons.x,
    "check"          -> icons.check,
    "plus"           -> icons.plus,
    "minus"          -> icons.minus,
    "search"         -> icons.search,
    "menu"           -> icons.menu,
    "moreHorizontal" -> icons.moreHorizontal,
    "filter"         -> icons.filter,
    "refresh"        -> icons.refresh,
    "info"           -> icons.info,
    "alertTriangle"  -> icons.alertTriangle,
    "alertCircle"    -> icons.alertCircle,
    "checkCircle"    -> icons.checkCircle,
    "xCircle"        -> icons.xCircle,
    "eye"            -> icons.eye,
    "pencil"         -> icons.pencil,
    "trash"          -> icons.trash,
    "copy"           -> icons.copy,
    "download"       -> icons.download,
    "upload"         -> icons.upload,
    "file"           -> icons.file,
    "folder"         -> icons.folder,
    "link"           -> icons.link,
    "home"           -> icons.home,
    "user"           -> icons.user,
    "users"          -> icons.users,
    "settings"       -> icons.settings,
    "logOut"         -> icons.logOut,
    "lock"           -> icons.lock,
    "sun"            -> icons.sun,
    "moon"           -> icons.moon,
    "star"           -> icons.star,
    "heart"          -> icons.heart,
    "bell"           -> icons.bell,
    "calendar"       -> icons.calendar,
    "mail"           -> icons.mail,
    "apps"           -> icons.apps
  )

  /** Renders a single glyph in a labeled tile. */
  private def glyphTile(name: String, glyph: SvgElement): HtmlElement =
    div(
      themed(t =>
        stack.col(spacing.sm) ++
          css.alignItems("center") ++
          css.padding(spacing.md) ++
          css.borderRadius(radius.md) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.background(t.surface)
      ),
      div(
        ThemedStyle(t => css.color(t.text) ++ css.width(Length.px(24)) ++ css.height(Length.px(24))),
        glyph
      ),
      span(typo.hint, name)
    )

  def lucide(): HtmlElement = PageTemplate(
    title = "icons.* (Lucide)",
    summary = "A curated set of stroke-based 24×24 SVG glyphs from lucide.dev (MIT). " +
      "Each uses currentColor, so the parent's css.color(...) tints them. Wrap with Icon for sizing."
  )(
    PageTemplate.section("Usage")(
      PageTemplate.paragraph(
        "Each glyph is an SvgElement. Drop it directly into a tag, or pass through Icon(size, color) " +
          "for the standard inline-flex + sized wrapper:"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// Bare: inherits color and fills its parent box
            |span(themed(t => css.color(t.brand)), icons.check)
            |
            |// Sized via Icon (recommended in buttons / inline text)
            |Icon(size = Length.px(20))(icons.search)
            |Icon(size = Length.px(16), color = Some(palette.red600))(icons.trash)
            |
            |// Pair with text in a button
            |Button(
            |  Button.label := "",
            |  Button.variant := Button.Variant.Secondary
            |)
            |  // ...or compose a custom row:
            |div(
            |  stack.row(spacing.sm) ++ css.alignItems("center"),
            |  Icon(size = Length.px(16))(icons.download),
            |  span("Export CSV")
            |)""".stripMargin
      )
    ),
    PageTemplate.section("All shipped glyphs")(
      div(
        css.display(Display.Grid) ++
          css.raw("grid-template-columns", "repeat(auto-fill, minmax(120px, 1fr))") ++
          css.gap(spacing.md),
        lucideEntries.map { case (name, glyph) => glyphTile(name, glyph) }
      )
    ),
    PageTemplate.section("Colors")(
      PageTemplate.codedDemo(
        "Recolor via the parent",
        """div(themed(t => css.color(t.brand)),   Icon(size = Length.px(20))(icons.checkCircle))
          |div(themed(t => css.color(t.success)), Icon(size = Length.px(20))(icons.checkCircle))
          |div(themed(t => css.color(t.warning)), Icon(size = Length.px(20))(icons.alertTriangle))
          |div(themed(t => css.color(t.danger)),  Icon(size = Length.px(20))(icons.xCircle))""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          div(themed(t => css.color(t.brand)),   Icon(size = Length.px(20))(icons.checkCircle)),
          div(themed(t => css.color(t.success)), Icon(size = Length.px(20))(icons.checkCircle)),
          div(themed(t => css.color(t.warning)), Icon(size = Length.px(20))(icons.alertTriangle)),
          div(themed(t => css.color(t.danger)),  Icon(size = Length.px(20))(icons.xCircle))
        )
      )
    ),
    PageTemplate.behavior(
      "All glyphs are 24×24 with stroke-width 2, round line caps and joins.",
      "fill is none and stroke is currentColor — color inherits from the parent.",
      "width and height default to 100%, so the icon fills the wrapping box. Use the Icon factory to pin a size.",
      "Add new glyphs by copying their <path>/<polyline>/<circle>/<line>/<rect> elements from lucide.dev into icons.scala."
    )
  )

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
