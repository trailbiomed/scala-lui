package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.*
import lui.style.*
import lui.components.*

object FoundationsPages {

  // ---------------------------------------------------------------------------
  def theme(): HtmlElement = PageTemplate(
    title = "Theme tokens",
    summary = "Semantic color tokens that components reference. Swapping themes updates all of them at once."
  )(
    PageTemplate.section("Surfaces")(
      swatchRow(Seq(
        "t.bg"          -> Theme.current.now().bg,
        "t.surface"     -> Theme.current.now().surface,
        "t.surfaceDim"  -> Theme.current.now().surfaceDim,
        "t.backdrop"    -> Theme.current.now().backdrop
      ))
    ),
    PageTemplate.section("Borders")(
      swatchRow(Seq(
        "t.border"        -> Theme.current.now().border,
        "t.borderActive"  -> Theme.current.now().borderActive
      ))
    ),
    PageTemplate.section("Text")(
      swatchRow(Seq(
        "t.text"        -> Theme.current.now().text,
        "t.textMuted"   -> Theme.current.now().textMuted,
        "t.textSubtle"  -> Theme.current.now().textSubtle
      ))
    ),
    PageTemplate.section("Brand")(
      swatchRow(Seq(
        "t.brand"      -> Theme.current.now().brand,
        "t.brandSoft"  -> Theme.current.now().brandSoft,
        "t.brandHover" -> Theme.current.now().brandHover,
        "t.onBrand"    -> Theme.current.now().onBrand
      ))
    ),
    PageTemplate.section("Status: success, warning, danger, info")(
      swatchRow(Seq(
        "t.success"        -> Theme.current.now().success,
        "t.successSoft"    -> Theme.current.now().successSoft,
        "t.successBorder"  -> Theme.current.now().successBorder
      )),
      swatchRow(Seq(
        "t.warning"        -> Theme.current.now().warning,
        "t.warningSoft"    -> Theme.current.now().warningSoft,
        "t.warningBorder"  -> Theme.current.now().warningBorder
      )),
      swatchRow(Seq(
        "t.danger"        -> Theme.current.now().danger,
        "t.dangerSoft"    -> Theme.current.now().dangerSoft,
        "t.dangerBorder"  -> Theme.current.now().dangerBorder
      )),
      swatchRow(Seq(
        "t.info"        -> Theme.current.now().info,
        "t.infoSoft"    -> Theme.current.now().infoSoft,
        "t.infoBorder"  -> Theme.current.now().infoBorder
      ))
    ),
    PageTemplate.section("Using tokens in a component")(
      PageTemplate.codedDemo(
        "themed(t => …)",
        """// inside a component, drive styles off the current theme:
          |div(
          |  themed(t =>
          |    css.background(t.surface) ++
          |      css.color(t.text) ++
          |      css.border(Length.px(1), BorderStyle.Solid, t.border)
          |  ),
          |  "Themed panel"
          |)""".stripMargin
      )(
        div(
          themed(t =>
            css.padding(spacing.lg) ++
              css.background(t.surface) ++
              css.color(t.text) ++
              css.border(Length.px(1), BorderStyle.Solid, t.border) ++
              css.borderRadius(radius.md)
          ),
          "Themed panel"
        )
      )
    ),
    PageTemplate.behavior(
      "All component pages flip live when you toggle the theme. This page does too.",
      "Theme.signal exposes the current Theme as a Laminar Signal. Components subscribe via signal.styled or themed(...).",
      "Set the theme with Theme.setLight() / Theme.setDark() / Theme.toggle()."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def palettePage(): HtmlElement = PageTemplate(
    title = "Color palette",
    summary = "Raw color stops backing the theme. Reach for these only when encoding data; for chrome, use the theme tokens."
  )(
    PageTemplate.section("Brand (teal)")(
      swatchRow(Seq(
        "teal50" -> palette.teal50, "teal100" -> palette.teal100, "teal200" -> palette.teal200,
        "teal400" -> palette.teal400, "teal500" -> palette.teal500, "teal600" -> palette.teal600,
        "teal700" -> palette.teal700, "teal900" -> palette.teal900
      ))
    ),
    PageTemplate.section("Neutrals (slate)")(
      swatchRow(Seq(
        "white" -> palette.white, "slate50" -> palette.slate50, "slate100" -> palette.slate100,
        "slate200" -> palette.slate200, "slate300" -> palette.slate300, "slate400" -> palette.slate400
      )),
      swatchRow(Seq(
        "slate500" -> palette.slate500, "slate600" -> palette.slate600, "slate700" -> palette.slate700,
        "slate800" -> palette.slate800, "slate900" -> palette.slate900
      ))
    ),
    PageTemplate.section("Emerald, red, blue, amber")(
      swatchRow(Seq("emerald50" -> palette.emerald50, "emerald300" -> palette.emerald300, "emerald600" -> palette.emerald600, "emerald700" -> palette.emerald700)),
      swatchRow(Seq("red50" -> palette.red50, "red300" -> palette.red300, "red600" -> palette.red600, "red800" -> palette.red800)),
      swatchRow(Seq("blue50" -> palette.blue50, "blue300" -> palette.blue300, "blue600" -> palette.blue600)),
      swatchRow(Seq("amber50" -> palette.amber50, "amber100" -> palette.amber100, "amber300" -> palette.amber300, "amber700" -> palette.amber700, "amber800" -> palette.amber800))
    ),
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "palette.*",
        """// raw palette colors: reach for these when encoding data,
          |// not for chrome (use theme tokens instead).
          |AnnotationStrip(
          |  AnnotationStrip.cells := samples.map(s =>
          |    if (s.healthy) palette.emerald600 else palette.red600
          |  )
          |)""".stripMargin
      )(
        div(stack.row(spacing.xs),
          ColorSwatch(palette.emerald600, Length.px(28)),
          ColorSwatch(palette.red600,     Length.px(28)),
          ColorSwatch(palette.amber700,   Length.px(28)),
          ColorSwatch(palette.blue600,    Length.px(28))
        )
      )
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def spacingPage(): HtmlElement = PageTemplate(
    title = "Spacing scale",
    summary = "A discrete scale used for padding, gap, and margin. Prefer these over ad-hoc Length.px values."
  )(
    PageTemplate.section("Tokens")(
      div(
        stack.col(spacing.md),
        spacingRow("xs",  spacing.xs),
        spacingRow("sm",  spacing.sm),
        spacingRow("md",  spacing.md),
        spacingRow("lg",  spacing.lg),
        spacingRow("xl",  spacing.xl),
        spacingRow("xxl", spacing.xxl),
        spacingRow("xxxl", spacing.xxxl)
      )
    ),
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "spacing.*",
        """div(
          |  stack.col(spacing.lg) ++ css.padding(spacing.xl),
          |  span("Heading"),
          |  span("Body text")
          |)""".stripMargin
      )(
        div(
          themed(t => css.background(t.surfaceDim) ++ css.borderRadius(radius.md)),
          stack.col(spacing.lg) ++ css.padding(spacing.xl),
          span(typo.label, "Heading"),
          span(typo.body, "Body text")
        )
      )
    ),
    PageTemplate.behavior(
      "Each step is roughly the next standard unit (4, 6, 8, 12, 16, 24, 32 px).",
      "Match the gap on a stack to the padding of its parent for visually consistent rhythm."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def radiusPage(): HtmlElement = PageTemplate(
    title = "Border radius",
    summary = "Corner-rounding tokens used across components."
  )(
    PageTemplate.section("Tokens")(
      div(
        stack.row(spacing.lg) ++ stack.wrap,
        radiusTile("sm",   radius.sm),
        radiusTile("md",   radius.md),
        radiusTile("lg",   radius.lg),
        radiusTile("xl",   radius.xl),
        radiusTile("pill", radius.pill)
      )
    ),
    PageTemplate.section("In context")(
      PageTemplate.paragraph(
        "What each token is typically used for. Hover the swatches above to see the difference; below are the conventions in lui."
      ),
      Table(
        Table.columns := Seq("Token", "CSS", "Typical use"),
        Table.rows := Seq(
          Seq("radius.sm",   radius.sm.toCss,   "Tags, inline chips, small inputs, code chips."),
          Seq("radius.md",   radius.md.toCss,   "Buttons, text inputs, dropdowns, popovers, alerts."),
          Seq("radius.lg",   radius.lg.toCss,   "Cards, dropzones, drawers, popover-as-card."),
          Seq("radius.xl",   radius.xl.toCss,   "Hero surfaces (surface.card)."),
          Seq("radius.pill", radius.pill.toCss, "Avatars, status dots, slider thumbs, brand-pill badges.")
        )
      ),
      Heading(4)("Live components"),
      div(
        stack.row(spacing.lg) ++ stack.wrap,
        Tag(Tag.label := "radius.sm",   Tag.variant := Tag.Variant.Neutral),
        Button(Button.label := "radius.md"),
        Card(Card.padding := spacing.lg, Card.children(
          span(typo.label, "radius.lg"),
          span(typo.muted, "Card uses surface.card with radius.xl; standalone surfaces use radius.lg.")
        )),
        Avatar(Avatar.name := "radius.pill", Avatar.size := Avatar.Size.Lg)
      )
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def fontSizesPage(): HtmlElement = PageTemplate(
    title = "Type scale",
    summary = "Font-size tokens. Mostly used internally by typo.* presets and components."
  )(
    PageTemplate.section("Sizes")(
      div(
        stack.col(spacing.sm),
        fontRow("xs",     fontSizes.xs,     "Eyebrows, hints"),
        fontRow("sm",     fontSizes.sm,     "Hints"),
        fontRow("md",     fontSizes.md,     "Muted text"),
        fontRow("lg",     fontSizes.lg,     "Body, labels"),
        fontRow("xl",     fontSizes.xl,     "Buttons, inputs"),
        fontRow("xxl",    fontSizes.xxl,    "Headings (h2)"),
        fontRow("xxxl",   fontSizes.xxxl,   "Section titles"),
        fontRow("display", fontSizes.display, "Page headings (h1)")
      )
    ),
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "fontSizes.*",
        """span(css.fontSize(fontSizes.xxl), "Large heading")""".stripMargin
      )(
        div(stack.col(spacing.sm),
          span(css.fontSize(fontSizes.xs), "fontSizes.xs"),
          span(css.fontSize(fontSizes.lg), "fontSizes.lg"),
          span(css.fontSize(fontSizes.xxl), "fontSizes.xxl"),
          span(css.fontSize(fontSizes.display), "fontSizes.display")
        )
      )
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def breakpointsPage(): HtmlElement = PageTemplate(
    title = "Breakpoints",
    summary = "Responsive thresholds. Use with Device.viewportWidth to switch layouts."
  )(
    PageTemplate.section("Tokens")(
      Table(
        Table.columns := Seq("Name", "Width"),
        Table.rows := Seq(
          Seq("sm", breakpoints.sm.toCss),
          Seq("md", breakpoints.md.toCss),
          Seq("lg", breakpoints.lg.toCss),
          Seq("xl", breakpoints.xl.toCss)
        )
      )
    ),
    PageTemplate.behavior(
      "Use Device.viewportWidth.signal.map(_ < breakpoints.md.toPx) to drive layout decisions reactively.",
      "Prefer flex- and grid-based responsive layouts (SimpleGrid.autoFit, Wrap) over breakpoint switches when possible."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def length(): HtmlElement = PageTemplate(
    title = "Length factories",
    summary = "Strongly-typed CSS lengths. Construct via Length.px / Length.pct / Length.em / Length.rem."
  )(
    PageTemplate.section("Examples")(
      Table(
        Table.columns := Seq("Call", "CSS"),
        Table.rows := Seq(
          Seq("Length.px(16)",    Length.px(16).toCss),
          Seq("Length.px(1.5)",   Length.px(1.5).toCss),
          Seq("Length.pct(50)",   Length.pct(50).toCss),
          Seq("Length.em(0.05)",  Length.em(0.05).toCss),
          Seq("Length.rem(1.5)",  Length.rem(1.5).toCss),
          Seq("Length.auto",      Length.auto.toCss),
          Seq("Length.zero",      Length.zero.toCss)
        )
      )
    ),
    PageTemplate.section("From a CSS literal")(
      PageTemplate.paragraph(
        "`Length` is opaque over `String`, so a bare literal does not typecheck and the " +
          "error does not mention `Length.px`. An opt-in conversion covers the cases where " +
          "a literal reads better: bring `scala.language.implicitConversions` into scope " +
          "and `TextInput.width := \"120px\"` compiles. Without that import the compiler " +
          "still demands a `Length`."
      ),
      codedDemo(
        "given Conversion[String, Length]",
        """import scala.language.implicitConversions
          |
          |TextInput(TextInput.width := "120px")   // via the conversion
          |TextInput(TextInput.width := Length.px(120))   // always
          |Length.raw("clamp(8rem, 30vw, 20rem)")  // anything the factories don't cover""".stripMargin
      )(
        div(
          stack.col(spacing.xs),
          span(typo.hint, s"""Length.raw("clamp(8rem, 30vw, 20rem)") = ${Length.raw("clamp(8rem, 30vw, 20rem)").toCss}""")
        )
      )
    ),
    PageTemplate.behavior(
      "Prefer `Length.px(n)` outside the tokens module. Laminar's CSS-length traits also export `.px` on Int, and that one tends to shadow ours.",
      "Length is an opaque type aliased to String. `.toCss` returns the underlying CSS value.",
      "`Length.raw` is the escape hatch for a value the factories don't cover — calc(), clamp(), viewport units."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def color(): HtmlElement = PageTemplate(
    title = "Color",
    summary = "RGB(A) color value type with alpha tweaking. Construct via Color.hex or Color(r, g, b, a)."
  )(
    PageTemplate.section("Examples")(
      Table(
        Table.columns := Seq("Call", "CSS"),
        Table.rows := Seq(
          Seq("""Color.hex("#0d9488")""",     Color.hex("#0d9488").toCss),
          Seq("Color(20, 184, 166)",           Color(20, 184, 166).toCss),
          Seq("Color(20, 184, 166, 0.18)",     Color(20, 184, 166, 0.18).toCss),
          Seq("palette.teal600.alpha(0.4)",   palette.teal600.alpha(0.4).toCss),
          Seq("Color.transparent",             Color.transparent.toCss)
        )
      )
    ),
    PageTemplate.section("Alpha demo")(
      div(
        stack.row(spacing.md),
        Seq(1.0, 0.7, 0.45, 0.2).map(a =>
          ColorSwatch(palette.teal600.alpha(a), Length.px(48), rounded = true)
        )
      )
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def cssBuilders(): HtmlElement = PageTemplate(
    title = "css.* builders",
    summary = "Typed Style builders. Each returns a one-decl Style; compose with ++."
  )(
    PageTemplate.section("Common builders")(
      Table(
        Table.columns := Seq("Builder", "What it sets"),
        Table.rows := Seq(
          Seq("css.background(Color)",          "background"),
          Seq("css.color(Color)",               "color"),
          Seq("css.padding(Length)",            "padding"),
          Seq("css.padding(v, h)",              "padding (two-axis)"),
          Seq("css.margin(Length)",             "margin"),
          Seq("css.gap(Length)",                "gap"),
          Seq("css.width / height / maxHeight", "sizing"),
          Seq("css.border(w, style, color)",    "border shorthand"),
          Seq("css.borderRadius(Length)",       "border-radius"),
          Seq("css.fontSize / fontWeight",      "type"),
          Seq("css.textAlign(TextAlign)",       "text-align"),
          Seq("css.display(Display)",           "display"),
          Seq("css.alignItems / justifyContent","flex alignment"),
          Seq("css.position / top / left / …",  "position"),
          Seq("css.transition(prop, ms)",       "transition shorthand"),
          Seq("""css.raw("prop", "value")""",   "escape hatch for anything else")
        )
      )
    ),
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "css.* + ++",
        """div(
          |  css.padding(spacing.lg) ++
          |    css.background(palette.teal600) ++
          |    css.color(palette.white) ++
          |    css.borderRadius(radius.md),
          |  "Composed inline"
          |)""".stripMargin
      )(
        div(
          css.padding(spacing.lg) ++ css.background(palette.teal600) ++ css.color(palette.white) ++ css.borderRadius(radius.md),
          "Composed inline"
        )
      )
    ),
    PageTemplate.behavior(
      "Import as namespaced. A wildcard `import lui.style.css.*` tends to collide with Laminar's identically-named property setters.",
      "Reach for `css.raw(\"prop\", \"value\")` when no typed builder exists. If the property is common, consider adding a typed builder."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def style(): HtmlElement = PageTemplate(
    title = "Style, ThemedStyle, themed(...)",
    summary = "How declarations compose into Modifier[HtmlElement]s."
  )(
    PageTemplate.section("Three layers")(
      Listing()(
        Listing.item(span(typo.body, b("Style"), ": a vector of CSS decls. Compose with ", b("++"), ". Drops directly into any tag.")),
        Listing.item(span(typo.body, b("ThemedStyle"), ": a Theme ⇒ Style function. Built with ", b("themed(t => …)"), " or via typo.*/surface.* presets. Also a Modifier.")),
        Listing.item(span(typo.body, b("signal.styled((t, a) => Style)"), ": for state-driven styles. Returns a Modifier that re-emits inline style on theme + state changes."))
      )
    ),
    PageTemplate.section("Demo")(
      div(
        stack.row(spacing.lg),
        // Static
        div(
          css.padding(spacing.lg) ++ css.background(palette.teal600) ++ css.color(palette.white) ++ css.borderRadius(radius.md),
          "Static Style"
        ),
        // Themed
        div(
          themed(t => css.padding(spacing.lg) ++ css.background(t.brand) ++ css.color(t.onBrand) ++ css.borderRadius(radius.md)),
          "ThemedStyle via themed(...)"
        ),
        // typo + raw style composition
        div(
          surface.card ++ css.padding(spacing.lg),
          "Themed + static (surface.card ++ css.padding(...))"
        )
      )
    ),
    PageTemplate.section("Two styles on one element merge")(
      PageTemplate.paragraph(
        "Every lui style modifier — a static `Style`, a `ThemedStyle`, `signal.styled`, " +
          "`themed` — claims its own layer on the element and writes only the properties " +
          "it declares. Two of them therefore compose rather than race. Where both set " +
          "the same property, the later modifier wins, which is the same last-wins rule " +
          "that applies inside a single `Style`."
      ),
      codedDemo(
        "A layout Style beside a themed one",
        """div(
          |  stack.col(spacing.md),                    // layout, one layer
          |  surface.card ++ css.padding(spacing.lg),  // theme, another
          |  someSignal.styled((t, a) => …)            // state, a third
          |)
          |// All three apply. The later one wins only on a property they share.""".stripMargin
      )(
        div(
          stack.col(spacing.md),
          surface.card ++ css.padding(spacing.lg),
          span(typo.body, "A flex column…"),
          span(typo.body, "…inside a card surface. Both layers applied.")
        )
      ),
      PageTemplate.paragraph(
        "Composing with `++` into a single `Style` is still the clearest thing to write " +
          "when the decls belong together — it puts the last-wins resolution where a " +
          "reader can see it."
      )
    ),
    PageTemplate.behavior(
      "Composition rules: Style ++ Style = Style. ThemedStyle ++ Style (or vice versa) = ThemedStyle. Signal[Style] only via .styled.",
      "CSS last-wins inside a single Style. `typo.label ++ css.fontWeight(SemiBold)` upgrades the weight.",
      "Only the properties lui declared are ever removed, so a lui Style can sit alongside Laminar's own style setters on the same element.",
      "A dynamic layer that stops declaring a property releases it: a `styled` that emits `box-shadow` only while focused leaves whatever a lower layer set once focus goes."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def stackPage(): HtmlElement = PageTemplate(
    title = "stack.* presets",
    summary = "Pure-layout shortcuts for flex containers. No theme dependency."
  )(
    PageTemplate.section("Containers")(
      codedDemo(
        "stack.col(gap)",
        """div(
          |  stack.col(spacing.md),
          |  boxedItem("A"),
          |  boxedItem("B"),
          |  boxedItem("C")
          |)""".stripMargin
      )(
        div(stack.col(spacing.md), boxedItem("A"), boxedItem("B"), boxedItem("C"))
      ),
      codedDemo(
        "stack.row(gap)",
        """div(
          |  stack.row(spacing.md),
          |  boxedItem("A"),
          |  boxedItem("B"),
          |  boxedItem("C")
          |)""".stripMargin
      )(
        div(stack.row(spacing.md), boxedItem("A"), boxedItem("B"), boxedItem("C"))
      ),
      codedDemo(
        "stack.between(gap)",
        """div(
          |  stack.between(spacing.md),
          |  boxedItem("Left"),
          |  boxedItem("Right")
          |)""".stripMargin
      )(
        div(stack.between(spacing.md), boxedItem("Left"), boxedItem("Right"))
      ),
      codedDemo(
        "stack.centerAll",
        """div(
          |  stack.centerAll ++ css.height(Length.px(80)) ++ css.width(Length.pct(100)),
          |  boxedItem("Centered")
          |)""".stripMargin
      )(
        div(stack.centerAll ++ css.height(Length.px(80)) ++ css.width(Length.pct(100)), boxedItem("Centered"))
      )
    ),
    PageTemplate.section("Child verbs")(
      codedDemo(
        "stack.grow",
        """div(
          |  stack.row(spacing.md) ++ css.width(Length.pct(100)),
          |  boxedItem("fixed"),
          |  div(stack.grow, boxedItem("grow")),
          |  boxedItem("fixed")
          |)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.width(Length.pct(100)),
          boxedItem("fixed"),
          div(stack.grow, boxedItem("grow") ),
          boxedItem("fixed")
        )
      ),
      codedDemo(
        "stack.noShrink + stack.wrap",
        """div(
          |  stack.row(spacing.md) ++ stack.wrap ++ css.width(Length.pct(100)),
          |  (1 to 6).map(i => div(boxedItem(s"item $i"), stack.noShrink))
          |)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ stack.wrap ++ css.width(Length.pct(100)),
          (1 to 6).map(i => div(boxedItem(s"item $i"), stack.noShrink))
        )
      )
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def typoPage(): HtmlElement = PageTemplate(
    title = "typo.* presets",
    summary = "Themed text styles. Drop straight into any tag."
  )(
    PageTemplate.section("Presets")(
      PageTemplate.codedDemo(
        "typo.*",
        """span(typo.eyebrow, "Eyebrow")
          |span(typo.h1, "Heading 1")
          |span(typo.h2, "Heading 2")
          |span(typo.label, "Label / field title")
          |span(typo.body, "Body, the default reading style.")
          |span(typo.muted, "Muted, secondary information.")
          |span(typo.hint, "Hint, tertiary helper text.")""".stripMargin
      )(
        div(
          stack.col(spacing.md),
          span(typo.eyebrow, "Eyebrow"),
          span(typo.h1, "Heading 1"),
          span(typo.h2, "Heading 2"),
          span(typo.label, "Label / field title"),
          span(typo.body, "Body, the default reading style."),
          span(typo.muted, "Muted, secondary information."),
          span(typo.hint,  "Hint, tertiary helper text.")
        )
      )
    ),
    PageTemplate.behavior(
      "Each preset is a ThemedStyle, so it composes with ++.",
      "Override individual decls by appending: `typo.label ++ css.fontWeight(FontWeight.SemiBold)`.",
      "`hint` and `eyebrow` resolve their color through `textMuted`, not `textSubtle`. Both carry real text — a field's hint, a caveat, a provenance line, a section label — and `textSubtle` sits below WCAG AA on a light surface by design.",
      "`textSubtle` is the decoration step of the scale: separators, out-of-month days, disabled labels. Reach for `t.textSubtle` directly when that is what you mean.",
      "`hint` is one type size below `muted`, so it stays visually subordinate without going illegible."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def surfacePage(): HtmlElement = PageTemplate(
    title = "surface.* presets",
    summary = "Themed surface backgrounds. Card is the elevated white panel; dim is the recessed grey panel."
  )(
    PageTemplate.section("See also")(
      PageTemplate.paragraph(
        "Layout / Surface — the capitalized `Surface.interactive(...)` builder. Use this lowercase " +
          "`surface.*` preset to style elements you build yourself; use `Surface` when you want a ready-made clickable card."
      )
    ),
    PageTemplate.paragraph(
      "Both surfaces are drawn here on the page background (t.bg). On a default demo card, which is t.surface, `surface.card` would be invisible because it matches the background. Always remember which layer a surface lives on."
    ),
    PageTemplate.section("Side by side, on t.bg")(
      div(
        themed(t =>
          stack.row(spacing.lg) ++ css.padding(spacing.xl) ++ css.background(t.bg) ++ css.borderRadius(radius.md)
        ),
        div(
          surface.card ++ css.padding(spacing.xl) ++ css.raw("flex", "1 1 0"),
          span(typo.label, "surface.card"),
          br(),
          span(typo.muted, "Elevated paper panel. t.surface background, 1.5px border, radius.xl.")
        ),
        div(
          surface.dim ++ css.padding(spacing.xl) ++ css.raw("flex", "1 1 0"),
          span(typo.label, "surface.dim"),
          br(),
          span(typo.muted, "Recessed grey panel. t.surfaceDim background, 1px border, radius.md.")
        )
      )
    ),
    PageTemplate.section("Same surfaces, on t.surface")(
      PageTemplate.paragraph(
        "On a t.surface parent (like the default demo card), surface.card vanishes into the page; surface.dim still reads as recessed."
      ),
      div(
        themed(t =>
          stack.row(spacing.lg) ++ css.padding(spacing.xl) ++ css.background(t.surface) ++ css.borderRadius(radius.md) ++ css.border(Length.px(1), BorderStyle.Solid, t.border)
        ),
        div(
          surface.card ++ css.padding(spacing.xl) ++ css.raw("flex", "1 1 0"),
          span(typo.label, "surface.card"),
          br(),
          span(typo.muted, "Disappears into the parent.")
        ),
        div(
          surface.dim ++ css.padding(spacing.xl) ++ css.raw("flex", "1 1 0"),
          span(typo.label, "surface.dim"),
          br(),
          span(typo.muted, "Still visibly recessed.")
        )
      )
    ),
    PageTemplate.behavior(
      "Use surface.card for the main panels of a page (run cards, reference cards).",
      "Use surface.dim for grouped sub-content inside a card. Accordion bodies, code blocks, callouts."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def interactive(): HtmlElement = PageTemplate(
    title = "Interactive",
    summary = "Per-element reactive hover / focus / pressed state, exposed as a Signal."
  )(
    PageTemplate.section("Inside a Component")(
      PageTemplate.paragraph(
        "Components expose a lazy `interact` value that installs hover/focus/pressed listeners on first access. " +
          "You can drive styles off `el.interact.state.styled`."
      ),
      codedDemo(
        "el.interact.state",
        """root.amend(
          |  el.interact.state.styled { (t, i) =>
          |    val bd =
          |      if (i.pressed)        t.brand
          |      else if (i.focused)   t.borderActive
          |      else if (i.hovered)   t.borderActive
          |      else                  t.border
          |    css.padding(spacing.xl) ++
          |      css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          |      css.borderRadius(radius.lg) ++
          |      css.background(if (i.pressed) t.brandSoft else t.surface) ++
          |      css.cursor("pointer") ++
          |      css.transition("border-color", 120)
          |  }
          |)""".stripMargin
      ) {
        val host = div()
        val ix = Interactive.on(host)
        host.amend(
          ix.state.styled { (t, i) =>
            val bd =
              if (i.pressed) t.brand
              else if (i.focused) t.borderActive
              else if (i.hovered) t.borderActive
              else t.border
            css.padding(spacing.xl) ++
              css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
              css.borderRadius(radius.lg) ++
              css.background(if (i.pressed) t.brandSoft else t.surface) ++
              css.cursor("pointer") ++
              css.transition("border-color", 120)
          },
          tabIndex := 0,
          "Hover, focus, press"
        )
        host
      }
    ),
    PageTemplate.section("On a plain Laminar element")(
      PageTemplate.paragraph(
        "Use `Interactive.on(host)` to attach the same Vars to any HtmlElement — useful when you need hover state outside a Component (e.g. inside `Surface.interactive`)."
      ),
      codedDemo(
        "Interactive.on(host)",
        """val host = div()
          |val ix = Interactive.on(host)
          |host.amend(
          |  ix.state.styled { (t, i) =>
          |    css.padding(spacing.lg) ++
          |      css.background(if (i.hovered) t.brandSoft else t.surface) ++
          |      css.border(Length.px(1), BorderStyle.Solid,
          |        if (i.hovered) t.borderActive else t.border) ++
          |      css.borderRadius(radius.md) ++
          |      css.cursor("pointer")
          |  },
          |  "Hover me"
          |)""".stripMargin
      ) {
        val host = div()
        val ix = Interactive.on(host)
        host.amend(
          ix.state.styled { (t, i) =>
            css.padding(spacing.lg) ++
              css.background(if (i.hovered) t.brandSoft else t.surface) ++
              css.border(Length.px(1), BorderStyle.Solid,
                if (i.hovered) t.borderActive else t.border) ++
              css.borderRadius(radius.md) ++
              css.cursor("pointer")
          },
          "Hover me"
        )
        host
      }
    ),
    PageTemplate.section("focusVisible — rings that don't fire on click")(
      PageTemplate.paragraph(
        "`focused` is raw DOM focus, and a mouse click sets it. A ring drawn off `focused` " +
          "therefore appears on every click, which is not what anyone means by a focus " +
          "ring. `focusVisible` is `focused` narrowed to focus the keyboard put there — " +
          "the distinction CSS calls `:focus-visible`, which no inline style can express."
      ),
      PageTemplate.paragraph(
        "Draw rings off `focusVisible`; keep backgrounds and other \"this is the current " +
          "item\" affordances on `focused`. A menu opened by click and walked with the " +
          "arrow keys still has to shade the row you are on."
      ),
      codedDemo(
        "i.focusVisible",
        """ix.state.styled { (t, i) =>
          |  val ring =
          |    if (i.focusVisible && !i.pressed)
          |      css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.35).toCss}")
          |    else css.raw("box-shadow", "none")
          |  css.background(if (i.focused) t.brandSoft else t.surface) ++ ring
          |}""".stripMargin
      ) {
        val host = button(typ := "button")
        val ix = Interactive.on(host)
        host.amend(
          ix.state.styled { (t, i) =>
            val ring =
              if (i.focusVisible && !i.pressed)
                css.raw("box-shadow", s"0 0 0 3px ${t.brand.alpha(0.35).toCss}")
              else css.raw("box-shadow", "none")
            css.padding(spacing.md, spacing.xl) ++
              css.borderRadius(radius.md) ++
              css.border(Length.px(1), BorderStyle.Solid, t.border) ++
              css.background(if (i.focused) t.brandSoft else t.surface) ++
              css.color(t.text) ++
              css.fontSize(fontSizes.xl) ++
              css.raw("font-family", "inherit") ++
              css.cursor("pointer") ++
              css.raw("outline", "none") ++
              ring
          },
          "Click me, then Tab to me"
        )
        div(
          stack.col(spacing.md),
          host,
          span(typo.hint, "Clicking tints the background but draws no ring. Tabbing to it does both.")
        )
      }
    ),
    PageTemplate.behavior(
      "InteractionState exposes four Booleans: `hovered`, `focused`, `pressed`, `focusVisible`. Subscribe via `interact.state` for the whole record.",
      "Individual Vars are also exposed: `interact.hovered.signal`, `interact.focused.signal`, `interact.pressed.signal`.",
      "`focusVisible` is derived, not tracked: it is `focused && Device.keyboardMode`, so putting the mouse down and then reaching for the keyboard makes the ring appear on the element that already has focus.",
      "`Device.keyboardMode` counts any keydown but a bare modifier as keyboard navigation, and any pointer press as not — on pointerdown, because focus lands there. Both listeners are on the capture phase so the mode is settled before any element's own focus handler runs.",
      "For a component that tracks focus in its own Var rather than through `Interactive`, `Interactive.focusVisible(focusedSignal)` applies the same narrowing.",
      "Text fields are the deliberate exception: TextInput, Textarea, NumberInput, PasswordInput and TagsInput still ring on plain `focused`, because clicking into one is a deliberate act of placing a caret and every native input rings on click.",
      "Listeners install only on first access of `el.interact`, so non-interactive components pay nothing for the lazy."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  def contrast(): HtmlElement = PageTemplate(
    title = "Contrast",
    summary = "WCAG 2.1 contrast arithmetic over Color, plus an audit of the pairings lui's own components draw."
  )(
    PageTemplate.section("Why it lives here")(
      PageTemplate.paragraph(
        "lui owns both halves of every pairing a component makes — the foreground token " +
          "and the surface it lands on. A consumer can pick a different component but not " +
          "a different pair, so a failing pairing can only be fixed here. `Contrast.audit` " +
          "is checked in lui's own test suite, which is what keeps a token change from " +
          "quietly dropping a pairing below AA."
      ),
      codedDemo(
        "Contrast.ratio / .failures",
        """Contrast.ratio(t.onBrand, t.brand)   // 5.47
          |Contrast.aa                          // 4.5
          |Contrast.failures(Theme.light)       // Nil
          |
          |// Every shipped theme, gated in core's test suite:
          |Theme.all.foreach(t => assert(Contrast.failures(t).isEmpty))""".stripMargin
      ) {
        div(
          stack.col(spacing.sm),
          children <-- Theme.signal.map { t =>
            val worst = Contrast.audit(t).sortBy(_.measured).take(6)
            worst.map { p =>
              div(
                stack.row(spacing.md),
                span(typo.label ++ css.width(Length.px(220)), s"${p.foreground} on ${p.background}"),
                span(typo.body, f"${p.measured}%.2f:1"),
                span(typo.hint, if (p.passes) "AA" else "below AA")
              )
            }.toList
          }
        )
      },
      PageTemplate.paragraph(
        "The six tightest pairings in the active theme, measured live. Switch the theme to " +
          "re-measure."
      )
    ),
    PageTemplate.section("Enumerating the tokens")(
      PageTemplate.paragraph(
        "`Theme.toMap` and `palette.all` expose every token by name, so an external gate " +
          "can read the real values instead of transcribing them. `Theme.all` lists the " +
          "themes lui ships."
      ),
      codedDemo(
        "Theme.toMap / palette.all",
        """Theme.light.toMap("brand")   // "rgb(15, 118, 110)"
          |Theme.light.colors("brand")  // Color(15, 118, 110, 1.0)
          |palette.all("teal700")       // Color(15, 118, 110, 1.0)
          |Theme.all.map(_.name)        // Seq("light", "dark", "monokai")""".stripMargin
      ) {
        div(
          stack.col(spacing.xs),
          span(typo.hint, s"palette.all holds ${palette.all.size} stops."),
          span(typo.hint, child.text <-- Theme.signal.map(t => s"${t.name} exposes ${t.toMap.size} semantic tokens.")),
          span(typo.hint, child.text <-- Theme.signal.map(t => s"brand = ${t.toMap.getOrElse("brand", "")}"))
        )
      }
    ),
    PageTemplate.behavior(
      "`luminance` follows WCAG 2.1 relative luminance and flattens a translucent color onto white first.",
      "`ratio(fg, bg)` composites `fg` onto `bg`; flatten a translucent background yourself with `Color.over` before passing it.",
      "`textSubtle` is deliberately absent from the audit: it is the decoration step of the text scale — separators, out-of-month days, disabled labels. `typo.hint` and `typo.eyebrow` resolve through `textMuted` precisely so no preset puts real text at that contrast.",
      "Borders are not audited. WCAG 1.4.11 asks 3:1 of a control's visual boundary, which lui's resting border deliberately does not meet — the boundary of a resting field is not the affordance."
    ),
    PageTemplate.noProps
  )

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private def swatchRow(items: Seq[(String, Color)]): HtmlElement =
    div(
      stack.row(spacing.lg) ++ stack.wrap,
      items.map { case (name, c) =>
        div(
          stack.col(spacing.xs),
          ColorSwatch(c, Length.px(56)),
          span(typo.hint, name),
          span(typo.hint, c.toCss)
        )
      }
    )

  private def spacingRow(name: String, l: Length): HtmlElement =
    div(
      stack.row(spacing.lg) ++ css.alignItems("center"),
      span(typo.label ++ css.width(Length.px(80)), name),
      span(typo.hint ++ css.width(Length.px(80)), l.toCss),
      div(
        themed(t => css.background(t.brand) ++ css.height(Length.px(12)) ++ css.borderRadius(Length.px(2))),
        css.width(l)
      )
    )

  private def radiusTile(name: String, r: Length): HtmlElement =
    div(
      stack.col(spacing.xs),
      div(
        themed(t => css.background(t.brand) ++ css.color(t.onBrand)),
        css.width(Length.px(80)) ++ css.height(Length.px(80)) ++
          css.borderRadius(r) ++ stack.centerAll,
        span(typo.label, name)
      ),
      span(typo.hint, r.toCss)
    )

  private def fontRow(name: String, l: Length, hint: String): HtmlElement =
    div(
      stack.row(spacing.lg) ++ css.alignItems("baseline"),
      span(typo.hint ++ css.width(Length.px(70)), name),
      span(css.fontSize(l), "Aa Bb 0123"),
      span(typo.hint, l.toCss),
      span(typo.hint, hint)
    )

  private def codedDemo(label: String, code: String)(demo: HtmlElement): HtmlElement =
    PageTemplate.codedDemo(label, code)(demo)

  private def boxedItem(label: String): HtmlElement =
    div(
      themed(t =>
        css.padding(spacing.sm, spacing.md) ++
          css.background(t.surface) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.borderRadius(radius.sm) ++
          css.color(t.text)
      ),
      label
    )

  private def b(s: String): HtmlElement =
    span(themed(t => css.fontWeight(FontWeight.SemiBold) ++ css.color(t.text)), s)
}
