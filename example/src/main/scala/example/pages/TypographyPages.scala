package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object TypographyPages {

  def heading(): HtmlElement = PageTemplate(
    title = "Heading",
    summary = "Semantic h1 to h4 with the matching type scale."
  )(
    PageTemplate.section("Levels")(
      PageTemplate.codedDemo(
        "Heading(level)",
        """Heading(1)("Heading 1")
          |Heading(2)("Heading 2")
          |Heading(3)("Heading 3")
          |Heading(4)("Heading 4")""".stripMargin
      )(
        div(stack.col(spacing.sm),
          Heading(1)("Heading 1"),
          Heading(2)("Heading 2"),
          Heading(3)("Heading 3"),
          Heading(4)("Heading 4")
        )
      )
    ),
    PageTemplate.behavior(
      "Picks the matching HTML tag, so pass the right level for accessibility, not just for type size.",
      "Margin is reset to 0. Pair with stack.col(gap) for vertical spacing."
    ),
    PageTemplate.propsTable(
      ("level",   "Int",                 "1, 2, 3, or 4. Picks both the tag and the type scale."),
      ("content", "Modifier[HtmlElement]*", "Children to render inside the heading.")
    )
  )

  def text(): HtmlElement = PageTemplate(
    title = "Text",
    summary = "Themed spans for body, muted, hint, label, and eyebrow text."
  )(
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Text.*",
        """Text.eyebrow("EYEBROW")
          |Text.label("Label / field title")
          |Text.body("Body — the default reading style.")
          |Text.muted("Muted — secondary information.")
          |Text.hint("Hint — tertiary helper text.")""".stripMargin
      )(
        div(stack.col(spacing.sm),
          Text.eyebrow("EYEBROW"),
          Text.label("Label / field title"),
          Text.body("Body, the default reading style."),
          Text.muted("Muted, secondary information."),
          Text.hint("Hint, tertiary helper text.")
        )
      )
    ),
    PageTemplate.behavior(
      "Each variant is a thin wrapper over the matching typo.* preset.",
      "Use Text.body or just Text(...) inline. Reserve Heading for h1/h2/h3 cases."
    ),
    PageTemplate.propsTable(
      ("Text(...)",         "Modifier[HtmlElement]*", "Body text. Same as Text.body."),
      ("Text.body(...)",    "Modifier[HtmlElement]*", "Default reading style."),
      ("Text.muted(...)",   "Modifier[HtmlElement]*", "Secondary information."),
      ("Text.hint(...)",    "Modifier[HtmlElement]*", "Tertiary helper text."),
      ("Text.label(...)",   "Modifier[HtmlElement]*", "Form-field label sized text."),
      ("Text.eyebrow(...)", "Modifier[HtmlElement]*", "Small uppercase section label.")
    )
  )

  def link(): HtmlElement = PageTemplate(
    title = "Link",
    summary = "Themed anchor with brand, muted, and plain variants."
  )(
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Link.variant",
        """Link(Link.href := "#overview", Link.children(span("Brand link")))
          |Link(Link.href := "#overview",
          |  Link.variant := Link.Variant.Muted,
          |  Link.children(span("Muted link")))""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ stack.wrap,
          Link(Link.href := "#overview", Link.children(span("Brand link"))),
          Link(Link.href := "#overview", Link.variant := Link.Variant.Muted, Link.children(span("Muted link"))),
          Link(Link.href := "#overview", Link.variant := Link.Variant.Plain, Link.children(span("Plain link")))
        )
      )
    ),
    PageTemplate.section("External")(
      PageTemplate.codedDemo(
        "Link.external",
        """Link(
          |  Link.href := "https://laminar.dev",
          |  Link.external := true,
          |  Link.children(span("Open laminar.dev"))
          |)""".stripMargin
      )(
        Link(
          Link.href := "https://laminar.dev",
          Link.external := true,
          Link.children(span("Open laminar.dev (new tab)"))
        )
      )
    ),
    PageTemplate.section("Scroll target (in-page anchor, no URL hash)")(
      PageTemplate.codedDemo(
        "Link.scrollTarget",
        """Link(
          |  Link.scrollTarget := "scroll-demo-target",
          |  Link.variant      := Link.Variant.Chip,
          |  Link.children("Jump to anchor")
          |)
          |// elsewhere on the page:
          |div(idAttr := "scroll-demo-target", "I'm the target.")""".stripMargin
      )(
        div(stack.col(spacing.lg),
          Link(
            Link.scrollTarget := "scroll-demo-target",
            Link.variant := Link.Variant.Chip,
            Link.children("Jump to anchor below")
          ),
          div(themed(t => stack.col(spacing.md) ++ css.height(Length.px(180)) ++
              css.background(t.surfaceDim) ++ css.borderRadius(radius.md) ++ css.padding(spacing.lg)),
            span(typo.muted, "spacer space spacer"),
            span(typo.muted, "spacer space spacer"),
            span(typo.muted, "spacer space spacer")
          ),
          div(idAttr := "scroll-demo-target",
            themed(t => css.background(t.brandSoft) ++ css.padding(spacing.lg) ++ css.borderRadius(radius.md)),
            span(typo.label, "Target anchor — scrolled into view, no URL hash change.")
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("href",         "String",                 "Anchor target."),
      ("external",     "Boolean",                "If true, opens in a new tab with rel=noopener noreferrer."),
      ("variant",      "Brand|Muted|Plain|Chip", "Color treatment."),
      ("scrollTarget", "String",                 "Element id to scrollIntoView; preventDefaults the click so URL hash is unchanged."),
      ("scrolled",     "Out[String]",            "Emits the target id after each scroll."),
      ("children",     "Slot",                   "Anchor content.")
    )
  )

  def listing(): HtmlElement = PageTemplate(
    title = "Listing",
    summary = "Bulleted or numbered list. Named `Listing` to avoid shadowing scala.List."
  )(
    PageTemplate.section("Bulleted (default)")(
      PageTemplate.codedDemo(
        "Listing()",
        """Listing()(
          |  Listing.item("Composable inline styles"),
          |  Listing.item("No external CSS"),
          |  Listing.item("No npm")
          |)""".stripMargin
      )(
        Listing()(
          Listing.item("Composable inline styles"),
          Listing.item("No external CSS"),
          Listing.item("No npm")
        )
      )
    ),
    PageTemplate.section("Numbered")(
      PageTemplate.codedDemo(
        "Listing(Numbered)",
        """Listing(style = Listing.Style.Numbered)(
          |  Listing.item("Add the component file"),
          |  Listing.item("Document it"),
          |  Listing.item("Ship it")
          |)""".stripMargin
      )(
        Listing(style = Listing.Style.Numbered)(
          Listing.item("Add the component file"),
          Listing.item("Document it"),
          Listing.item("Ship it")
        )
      )
    ),
    PageTemplate.section("Plain (no markers)")(
      PageTemplate.codedDemo(
        "Listing(None)",
        """Listing(style = Listing.Style.None)(
          |  Listing.item("No bullets, good for inline nav."),
          |  Listing.item("Each item is just a styled <li>.")
          |)""".stripMargin
      )(
        Listing(style = Listing.Style.None)(
          Listing.item("No bullets, good for inline nav."),
          Listing.item("Each item is just a styled <li>.")
        )
      )
    ),
    PageTemplate.propsTable(
      ("style", "Bulleted|Numbered|None", "Marker style. Default Bulleted."),
      ("gap",   "Length",                 "Gap between items. Default spacing.sm.")
    )
  )

  def blockquote(): HtmlElement = PageTemplate(
    title = "Blockquote",
    summary = "Quoted block with a brand-color left border."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Blockquote",
        """Blockquote(
          |  span("'Make every state visible to the type checker' " +
          |    "is the design rule that makes the rest of this codebase possible.")
          |)""".stripMargin
      )(
        Blockquote(
          span("'Make every state visible to the type checker' is the design rule that makes the rest of this codebase possible.")
        )
      )
    ),
    PageTemplate.noProps
  )

  def mark(): HtmlElement = PageTemplate(
    title = "Mark",
    summary = "Yellow-highlighted span (uses theme warningSoft). The primitive: you wrap what you want highlighted yourself."
  )(
    PageTemplate.section("Inline highlight")(
      PageTemplate.codedDemo(
        "Mark",
        """span(typo.body,
          |  "Search returned ", Mark("12"), " matches for ",
          |  Mark("\"laminar\""), " in this document."
          |)""".stripMargin
      )(
        span(typo.body,
          "Search returned ",
          Mark("12"),
          " matches for ",
          Mark("\"laminar\""),
          " in this document."
        )
      )
    ),
    PageTemplate.section("Mark vs Highlight")(
      PageTemplate.paragraph(
        "Use Mark when you already know which spans to highlight (links, computed values, structured content). " +
          "Use Highlight when you have a free-form string and a query substring and want every match wrapped automatically. " +
          "Highlight is built on top of Mark."
      )
    ),
    PageTemplate.noProps
  )

  def em(): HtmlElement = PageTemplate(
    title = "Em",
    summary = "Themed <em> with italic styling."
  )(
    PageTemplate.section("Inline emphasis")(
      PageTemplate.codedDemo(
        "Em",
        """span(typo.body, "This argument is ", Em("really"), " load-bearing.")""".stripMargin
      )(
        span(typo.body, "This argument is ", Em("really"), " load-bearing.")
      )
    ),
    PageTemplate.noProps
  )

  def highlight(): HtmlElement = PageTemplate(
    title = "Highlight",
    summary = "Search-style helper that renders a string with every occurrence of a query substring wrapped in Mark."
  )(
    PageTemplate.section("Highlight vs Mark")(
      PageTemplate.paragraph(
        "Highlight is built on top of Mark. Use Mark when you already know which spans to highlight, " +
          "and Highlight when you have a free-form string and a search query and want every match wrapped automatically."
      )
    ),
    PageTemplate.section("Substring search")(
      PageTemplate.codedDemo(
        "Highlight(text, query)",
        """Highlight("The quick brown fox jumps over the lazy dog.", "the")
          |Highlight("k-means, k-NN, k-medoids", "k-")""".stripMargin
      )(
        div(stack.col(spacing.sm),
          Highlight("The quick brown fox jumps over the lazy dog.", "the"),
          Highlight("k-means, k-NN, k-medoids", "k-")
        )
      )
    ),
    PageTemplate.behavior(
      "Case-insensitive match.",
      "Empty query passes the text through unmodified."
    ),
    PageTemplate.propsTable(
      ("text",  "String", "The full text to render."),
      ("query", "String", "The substring to wrap in Mark (case-insensitive).")
    )
  )

  def code(): HtmlElement = PageTemplate(
    title = "Code",
    summary = "Inline code chip and multi-line code block."
  )(
    PageTemplate.section("Inline")(
      PageTemplate.codedDemo(
        "Code (inline)",
        """span(typo.body, "Use ",
          |  Code(Code.text := "Vector[Int].fill(8)(0)"),
          |  " to allocate."
          |)""".stripMargin
      )(
        span(typo.body, "Use ", Code(Code.text := "Vector[Int].fill(8)(0)"), " to allocate.")
      )
    ),
    PageTemplate.section("Block")(
      PageTemplate.codedDemo(
        "Code.block := true",
        """Code(
          |  Code.block := true,
          |  Code.text := "val xs = Vector.fill(8)(0)\nval ys = xs.zipWithIndex"
          |)""".stripMargin
      )(
        Code(
          Code.block := true,
          Code.text := "val xs: Vector[Int] = Vector.fill(8)(0)\nval ys = xs.zipWithIndex.map { case (_, i) => i * i }\nys.sum"
        )
      )
    ),
    PageTemplate.section("Tinted variant")(
      PageTemplate.codedDemo(
        "Code.variant := Tinted",
        """Code(
          |  Code.block   := true,
          |  Code.variant := Code.Variant.Tinted,
          |  Code.text    := "val xs = Vector.fill(8)(0)"
          |)""".stripMargin
      )(
        Code(
          Code.block   := true,
          Code.variant := Code.Variant.Tinted,
          Code.text    := "val xs: Vector[Int] = Vector.fill(8)(0)\nval ys = xs.zipWithIndex.map { case (_, i) => i * i }\nys.sum"
        )
      )
    ),
    PageTemplate.propsTable(
      ("text",    "String",  "The code to render."),
      ("block",   "Boolean", "If true, multi-line preformatted block. Default false (inline chip)."),
      ("variant", "Boxed/Tinted", "Boxed (default): 1px border. Tinted: no border, tinted background, small radius, inherits font-size.")
    )
  )

  def kbd(): HtmlElement = PageTemplate(
    title = "Kbd",
    summary = "Visualizes a keyboard key."
  )(
    PageTemplate.section("Single key")(
      PageTemplate.codedDemo(
        "Kbd",
        """div(stack.row(spacing.sm),
          |  span(typo.body, "Press"),
          |  Kbd(Kbd.key := "K"),
          |  span(typo.body, "to open."))""".stripMargin
      )(
        div(stack.row(spacing.sm) ++ css.alignItems("center"),
          span(typo.body, "Press"), Kbd(Kbd.key := "K"), span(typo.body, "to open."))
      )
    ),
    PageTemplate.section("Combo")(
      PageTemplate.codedDemo(
        "Multi-key combo",
        """div(stack.row(spacing.xs),
          |  Kbd(Kbd.key := "⌘"),
          |  Kbd(Kbd.key := "Shift"),
          |  Kbd(Kbd.key := "Enter"))""".stripMargin
      )(
        div(stack.row(spacing.xs) ++ css.alignItems("center"),
          Kbd(Kbd.key := "⌘"), Kbd(Kbd.key := "Shift"), Kbd(Kbd.key := "Enter"))
      )
    ),
    PageTemplate.propsTable(
      ("key", "String", "Key label, e.g. \"K\", \"⌘\", \"Enter\".")
    )
  )
}
