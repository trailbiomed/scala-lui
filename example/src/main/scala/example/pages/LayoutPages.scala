package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object LayoutPages {

  def container(): HtmlElement = PageTemplate(
    title = "Container",
    summary = "Centered, max-width-bounded wrapper. Use for page-level content gutter."
  )(
    PageTemplate.section("Default")(
      PageTemplate.codedDemo(
        "Container()(...)",
        """Container()(
          |  div(themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.lg)),
          |    "I'm centered and have a max-width of 1100px.")
          |)""".stripMargin
      )(
        Container()(
          div(themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.lg) ++ css.borderRadius(radius.md)),
            "I'm centered and have a max-width of 1100px.")
        )
      )
    ),
    PageTemplate.section("Custom max-width and padding")(
      PageTemplate.codedDemo(
        "Container(maxWidth, pad)",
        """Container(maxWidth = Length.px(560), pad = spacing.xxl)(
          |  div("Container(maxWidth = 560px)")
          |)""".stripMargin
      )(
        Container(maxWidth = Length.px(560), pad = spacing.xxl)(
          div(themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.lg) ++ css.borderRadius(radius.md)),
            "Container(maxWidth = 560px)")
        )
      )
    ),
    PageTemplate.propsTable(
      ("maxWidth", "Length", "Outer max-width. Default 1100px."),
      ("pad",      "Length", "Symmetric padding. Default spacing.xl.")
    )
  )

  def center(): HtmlElement = PageTemplate(
    title = "Center",
    summary = "Centers a child on both axes."
  )(
    PageTemplate.section("Center.apply")(
      PageTemplate.codedDemo(
        "Center(...)",
        """Center(span(typo.label, "Hi"))""".stripMargin
      )(
        div(
          themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.lg) ++ css.borderRadius(radius.md) ++ css.height(Length.px(120))),
          Center(span(typo.label, "Hi"))
        )
      )
    ),
    PageTemplate.section("Center.absolute (parent should be position: relative)")(
      PageTemplate.codedDemo(
        "Center.absolute(...)",
        """div(css.position("relative"),
          |  Center.absolute(span(typo.label, "Floating"))
          |)""".stripMargin
      )(
        div(
          themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.lg) ++ css.borderRadius(radius.md) ++ css.height(Length.px(120)) ++ css.position("relative")),
          Center.absolute(span(typo.label, "Floating"))
        )
      )
    ),
    PageTemplate.noProps
  )

  def bleed(): HtmlElement = PageTemplate(
    title = "Bleed",
    summary = "Negative-margin escape hatch. Lets a child reach the edges of a padded parent."
  )(
    PageTemplate.section("Inline bleed inside a card")(
      PageTemplate.codedDemo(
        "Bleed",
        """div(surface.card ++ css.padding(spacing.xl),
          |  span(typo.body, "Above bleed"),
          |  Bleed(inline = spacing.xl)(
          |    div("I touch the card's left and right edges.")
          |  ),
          |  span(typo.body, "Below bleed")
          |)""".stripMargin
      )(
        div(
          surface.card ++ css.padding(spacing.xl),
          span(typo.body, "Above bleed"),
          Bleed(inline = spacing.xl)(
            div(themed(t => css.background(t.brandSoft) ++ css.padding(spacing.md)),
              "I touch the card's left and right edges.")
          ),
          span(typo.body, "Below bleed")
        )
      )
    ),
    PageTemplate.propsTable(
      ("inline", "Length", "Negative left/right margin."),
      ("block",  "Length", "Negative top/bottom margin.")
    )
  )

  def aspectRatio(): HtmlElement = PageTemplate(
    title = "Aspect Ratio",
    summary = "Preserve a width-to-height ratio for arbitrary content (images, embeds, placeholders)."
  )(
    PageTemplate.section("16:9 (default)")(
      PageTemplate.codedDemo(
        "AspectRatio()",
        """AspectRatio()(
          |  div(stack.centerAll ++ css.width(Length.pct(100)) ++ css.height(Length.pct(100)),
          |    "16 : 9")
          |)""".stripMargin
      )(
        div(css.maxWidth(Length.px(360)),
          AspectRatio()(
            div(themed(t => css.background(t.brandSoft) ++ stack.centerAll ++ css.width(Length.pct(100)) ++ css.height(Length.pct(100))),
              span(typo.label, "16 : 9"))
          )
        )
      )
    ),
    PageTemplate.section("1:1")(
      PageTemplate.codedDemo(
        "AspectRatio(ratio = 1.0)",
        """AspectRatio(ratio = 1.0)(
          |  div("1:1 square")
          |)""".stripMargin
      )(
        div(css.maxWidth(Length.px(160)),
          AspectRatio(ratio = 1.0)(
            div(themed(t => css.background(t.brand) ++ css.color(t.onBrand) ++ stack.centerAll ++ css.width(Length.pct(100)) ++ css.height(Length.pct(100))),
              span(typo.label, "1:1"))
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("ratio", "Double", "width / height. Default 16 / 9.")
    )
  )

  def wrap(): HtmlElement = PageTemplate(
    title = "Wrap",
    summary = "Flex row that wraps onto multiple lines, with a gap. Use for tag/chip rows."
  )(
    PageTemplate.section("Default gap")(
      PageTemplate.codedDemo(
        "Wrap()",
        """Wrap()(
          |  (1 to 10).map(i =>
          |    Tag(Tag.label := s"tag-$i", Tag.variant := Tag.Variant.Neutral)
          |  )
          |)""".stripMargin
      )(
        Wrap()(
          (1 to 10).map(i =>
            Tag(Tag.label := s"tag-$i", Tag.variant := Tag.Variant.Neutral)
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("gap",   "Length", "Row + column gap. Default spacing.md."),
      ("align", "String", "align-items value. Default \"center\".")
    )
  )

  def group(): HtmlElement = PageTemplate(
    title = "Group",
    summary = "Row with zero gap, for visually-attached controls."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Group(...)",
        """Group(
          |  Button(Button.label := "Cut",   Button.variant := Button.Variant.Secondary),
          |  Button(Button.label := "Copy",  Button.variant := Button.Variant.Secondary),
          |  Button(Button.label := "Paste", Button.variant := Button.Variant.Secondary)
          |)""".stripMargin
      )(
        Group(
          Button(Button.label := "Cut", Button.variant := Button.Variant.Secondary, Button.size := Button.Size.Small),
          Button(Button.label := "Copy", Button.variant := Button.Variant.Secondary, Button.size := Button.Size.Small),
          Button(Button.label := "Paste", Button.variant := Button.Variant.Secondary, Button.size := Button.Size.Small)
        )
      )
    ),
    PageTemplate.behavior(
      "Unlike Wrap, children have no gap and butt directly against each other.",
      "Caller is responsible for choosing children that share border styling."
    ),
    PageTemplate.noProps
  )

  def simpleGrid(): HtmlElement = PageTemplate(
    title = "SimpleGrid",
    summary = "CSS Grid with either N equal columns or auto-fit columns of a minimum width."
  )(
    PageTemplate.section("Fixed columns")(
      PageTemplate.codedDemo(
        "SimpleGrid(columns, gap)",
        """SimpleGrid(columns = 3, gap = spacing.lg)(
          |  (1 to 6).map(i => tile(s"$i"))
          |)""".stripMargin
      )(
        SimpleGrid(columns = 3, gap = spacing.lg)(
          (1 to 6).map(i =>
            div(themed(t => css.background(t.surfaceDim) ++ css.padding(spacing.md) ++ css.borderRadius(radius.sm) ++ css.textAlign(TextAlign.Center)),
              s"$i")
          )
        )
      )
    ),
    PageTemplate.section("Auto-fit columns")(
      PageTemplate.paragraph(
        "Auto-fit picks however many columns fit, each at least `minChildWidth` wide. Resize the window to see the column count change."
      ),
      PageTemplate.codedDemo(
        "SimpleGrid.autoFit(minChildWidth, gap)",
        """SimpleGrid.autoFit(
          |  minChildWidth = Length.px(180),
          |  gap = spacing.md
          |)(
          |  (1 to 8).map(i => card(s"Card $i"))
          |)""".stripMargin
      )(
        SimpleGrid.autoFit(minChildWidth = Length.px(180), gap = spacing.md)(
          (1 to 8).map(i =>
            div(themed(t => css.background(t.surface) ++ css.border(Length.px(1), BorderStyle.Solid, t.border) ++ css.padding(spacing.md) ++ css.borderRadius(radius.sm) ++ css.textAlign(TextAlign.Center)),
              s"Card $i")
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("columns",       "Int",    "Number of equal-width columns. Used by SimpleGrid(...)."),
      ("minChildWidth", "Length", "Minimum child width for auto-fit. Used by SimpleGrid.autoFit(...)."),
      ("gap",           "Length", "Row + column gap. Default spacing.lg.")
    )
  )

  def scrollArea(): HtmlElement = PageTemplate(
    title = "ScrollArea",
    summary = "Bounded scrollable container with native scrollbars."
  )(
    PageTemplate.section("Vertical (default)")(
      PageTemplate.codedDemo(
        "ScrollArea(maxHeight)",
        """ScrollArea(maxHeight = Length.px(180))(
          |  div(stack.col(spacing.sm),
          |    (1 to 30).map(i => span(typo.body, s"Item $i"))
          |  )
          |)""".stripMargin
      )(
        ScrollArea(maxHeight = Length.px(180))(
          div(stack.col(spacing.sm) ++ css.padding(spacing.md),
            (1 to 30).map(i => span(typo.body, s"Item $i"))
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("maxHeight", "Length",                                "Max-height of the scroll container."),
      ("direction", "\"vertical\" | \"horizontal\" | \"both\"", "Scroll axis. Default \"vertical\".")
    )
  )

  def divider(): HtmlElement = PageTemplate(
    title = "Divider",
    summary = "Horizontal or vertical separating line, optionally with a centered label."
  )(
    PageTemplate.section("Horizontal")(
      PageTemplate.codedDemo(
        "Divider()",
        """div(stack.col(spacing.lg),
          |  span(typo.body, "Above"),
          |  Divider(),
          |  span(typo.body, "Below")
          |)""".stripMargin
      )(
        div(stack.col(spacing.lg),
          span(typo.body, "Above"),
          Divider(),
          span(typo.body, "Below")
        )
      )
    ),
    PageTemplate.section("With label")(
      PageTemplate.codedDemo(
        "Divider(label)",
        """Divider(Divider.label := "section")""".stripMargin
      )(
        Divider(Divider.label := "section")
      )
    ),
    PageTemplate.section("Vertical")(
      PageTemplate.codedDemo(
        "Divider(orientation := Vertical)",
        """div(stack.row(spacing.md) ++ css.height(Length.px(40)),
          |  span(typo.body, "Left"),
          |  Divider(Divider.orientation := Divider.Orientation.Vertical),
          |  span(typo.body, "Right")
          |)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.height(Length.px(40)) ++ css.alignItems("stretch"),
          span(typo.body, "Left"),
          Divider(Divider.orientation := Divider.Orientation.Vertical),
          span(typo.body, "Right")
        )
      )
    ),
    PageTemplate.propsTable(
      ("orientation", "Horizontal|Vertical", "Layout direction. Default Horizontal."),
      ("label",       "String",              "Optional centered label.")
    )
  )

  def surfacePage(): HtmlElement = PageTemplate(
    title = "Surface",
    summary = "Builder for one-off clickable panels. Returns a plain HtmlElement, no component wrapper, no props."
  )(
    PageTemplate.section("See also")(
      PageTemplate.paragraph(
        "Foundations / surface presets — the lowercase `surface.card` and `surface.dim` " +
          "ThemedStyle values. Use those to style your own elements; use this `Surface` builder when you want a ready-made clickable card."
      )
    ),
    PageTemplate.section("What it is")(
      PageTemplate.paragraph(
        "Surface is a tiny utility object (not a component) with a single method, Surface.interactive. " +
          "It returns a themed div that highlights its border on hover and emits a click. Reach for it when " +
          "you need a clickable card-like shape inline, without building a dedicated Component."
      ),
      PageTemplate.paragraph(
        "Not the same as the `surface.*` presets in Foundations. Those are themed Style values " +
          "(surface.card, surface.dim) that you compose into your own styling. This Surface builds an actual element."
      )
    ),
    PageTemplate.section("Default")(
      PageTemplate.codedDemo(
        "Surface.interactive()",
        """Surface.interactive()(
          |  span(typo.label, "Click target"),
          |  span(typo.muted, "Border lights up on hover.")
          |)""".stripMargin
      )(
        Surface.interactive()(
          span(typo.label, "Click target"),
          span(typo.muted, "Border lights up on hover.")
        )
      )
    ),
    PageTemplate.section("Custom padding / radius / click sink")(
      PageTemplate.codedDemo(
        "Surface.interactive(pad, rad, click)",
        """val clicks = Var(0)
          |Surface.interactive(
          |  pad = spacing.xl,
          |  rad = radius.lg,
          |  click = clicks.updater[Unit]((c, _) => c + 1)
          |)(
          |  span(typo.label, "Click me"),
          |  span(typo.muted, child.text <-- clicks.signal.map(n => s"clicked $n times"))
          |)""".stripMargin
      )({
        val clicks = Var(0)
        Surface.interactive(
          pad = spacing.xl,
          rad = radius.lg,
          click = clicks.updater[Unit]((c, _) => c + 1)
        )(
          span(typo.label, "Click me"),
          span(typo.muted, child.text <-- clicks.signal.map(n => s"clicked $n times"))
        )
      })
    ),
    PageTemplate.behavior(
      "Used internally by `Card(interactive := true)` and by app-specific cards.",
      "Returns a plain HtmlElement, so you can't `.amend(prop := value)` after the fact."
    ),
    PageTemplate.propsTable(
      ("pad",   "Length",      "Inner padding. Default spacing.lg."),
      ("rad",   "Length",      "Corner radius. Default radius.lg."),
      ("click", "Sink[Unit]",  "Click event. Default no-op."),
      ("extra", "Style",       "Additional decls to layer on top of the default themed style. Default empty.")
    )
  )

  def actionBar(): HtmlElement = PageTemplate(
    title = "ActionBar",
    summary = "Sticky bottom-of-viewport bar for primary actions on a form or wizard page."
  )(
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "ActionBar(...)",
        """ActionBar(
          |  Button(Button.label := "Cancel",
          |    Button.variant := Button.Variant.Ghost),
          |  Button(Button.label := "Save",
          |    Button.variant := Button.Variant.Primary)
          |)""".stripMargin
      )(
        div(
          themed(t => css.border(Length.px(1), BorderStyle.Dashed, t.border) ++ css.borderRadius(radius.md) ++ css.padding(spacing.lg)),
          span(typo.muted, "Inline preview. Real ActionBar is position: fixed."),
          div(
            stack.row(spacing.md) ++ css.justifyContent("flex-end") ++ css.raw("margin-top", spacing.lg.toCss),
            Button(Button.label := "Cancel", Button.variant := Button.Variant.Ghost),
            Button(Button.label := "Save", Button.variant := Button.Variant.Primary)
          )
        )
      )
    ),
    PageTemplate.behavior(
      "Real ActionBar uses position: fixed with bottom: 0, full-width.",
      "Right-aligns its children in a row with a small drop shadow."
    ),
    PageTemplate.noProps
  )

  def visuallyHidden(): HtmlElement = PageTemplate(
    title = "VisuallyHidden",
    summary = "Renders text that's invisible to sighted users but discoverable by screen readers."
  )(
    PageTemplate.section("Usage")(
      PageTemplate.codedDemo(
        "VisuallyHidden",
        """IconButton(IconButton.icon := "✎", IconButton.ariaLabel := "Edit")
          |VisuallyHidden(span("Announced by screen readers, invisible to sighted users."))""".stripMargin
      )(
        div(
          stack.row(spacing.md),
          IconButton(IconButton.icon := "✎", IconButton.ariaLabel := "Edit"),
          VisuallyHidden(span("This text is announced by screen readers but not visible.")),
          span(typo.hint, "Inspect the DOM to see the hidden span.")
        )
      )
    ),
    PageTemplate.noProps
  )

  def skipNav(): HtmlElement = PageTemplate(
    title = "SkipNav",
    summary = "Keyboard-only \"skip to main content\" link. Appears when focused, otherwise invisible."
  )(
    PageTemplate.section("How to try it")(
      PageTemplate.paragraph(
        "Click the button below to put focus inside the demo, then press Tab. The next focusable element " +
          "is the SkipNav link. It will appear pinned to the top-left of the viewport."
      ),
      PageTemplate.codedDemo(
        "SkipNav(targetId, label)",
        """SkipNav("main-content")
          |// or:
          |SkipNav("main-content", label = "Jump to main content")""".stripMargin
      )(
        div(
          stack.col(spacing.md),
          Button(
            Button.label := "Click me, then press Tab",
            Button.variant := Button.Variant.Secondary
          ),
          SkipNav("main-content"),
          span(typo.hint, "The next focusable element is the SkipNav anchor.")
        )
      )
    ),
    PageTemplate.behavior(
      "Renders an `<a href=\"#{targetId}\">` that is visually hidden until focused, then becomes a fixed-position pill in the top-left.",
      "Place SkipNav as the very first focusable element in your app, typically right after the `<body>` tag, so the first Tab from a fresh page reveals it.",
      "The label is announced by screen readers even when invisible."
    ),
    PageTemplate.propsTable(
      ("targetId", "String", "The id of the main-content element to jump to."),
      ("label",    "String", "Visible/announced text. Default \"Skip to main content\".")
    )
  )
}
