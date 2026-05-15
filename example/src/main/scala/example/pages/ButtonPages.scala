package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object ButtonPages {

  def button(): HtmlElement = PageTemplate(
    title = "Button",
    summary = "A common action surface. Three variants: primary, secondary, ghost."
  )(
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Button.variant",
        """Button(Button.label := "Primary",   Button.variant := Button.Variant.Primary)
          |Button(Button.label := "Secondary", Button.variant := Button.Variant.Secondary)
          |Button(Button.label := "Ghost",     Button.variant := Button.Variant.Ghost)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ stack.wrap,
          Button(Button.label := "Primary",   Button.variant := Button.Variant.Primary),
          Button(Button.label := "Secondary", Button.variant := Button.Variant.Secondary),
          Button(Button.label := "Ghost",     Button.variant := Button.Variant.Ghost)
        )
      )
    ),
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Button.size",
        """Button(Button.label := "Small",  Button.size := Button.Size.Small)
          |Button(Button.label := "Medium", Button.size := Button.Size.Medium)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Button(Button.label := "Small",  Button.size := Button.Size.Small),
          Button(Button.label := "Medium", Button.size := Button.Size.Medium)
        )
      )
    ),
    PageTemplate.section("States")(
      PageTemplate.codedDemo(
        "Button.disabled / Button.loading",
        """Button(Button.label := "Disabled", Button.disabled := true)
          |Button(Button.label := "Loading…", Button.loading := true)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ stack.wrap,
          Button(Button.label := "Disabled", Button.disabled := true),
          Button(Button.label := "Loading…", Button.loading := true)
        )
      )
    ),
    PageTemplate.section("Click event")(
      PageTemplate.codedDemo(
        "Button.click",
        """val clicks = Var(0)
          |Button(
          |  Button.label := "Click me",
          |  Button.click.foreach(_ => clicks.update(_ + 1))
          |)""".stripMargin
      )({
        val clicks = Var(0)
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Button(Button.label := "Click me", Button.click.foreach(_ => clicks.update(_ + 1))),
          span(typo.muted, child.text <-- clicks.signal.map(n => s"clicked $n times"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("label",    "String",       "Button text."),
      ("variant",  "Primary|Secondary|Ghost", "Visual style."),
      ("size",     "Small|Medium",            "Size scale."),
      ("disabled", "Boolean",      "Disables interaction."),
      ("loading",  "Boolean",      "Shows a spinner; disables clicks."),
      ("click",    "Out[Unit]",    "Click event.")
    )
  )

  def iconButton(): HtmlElement = PageTemplate(
    title = "IconButton",
    summary = "A square button containing only an icon. Always pair with an ariaLabel."
  )(
    PageTemplate.section("Variants & sizes")(
      PageTemplate.codedDemo(
        "IconButton",
        """IconButton(IconButton.icon := "▶", IconButton.ariaLabel := "Play")
          |IconButton(IconButton.icon := "✎", IconButton.ariaLabel := "Edit",
          |  IconButton.variant := IconButton.Variant.Secondary)
          |IconButton(IconButton.icon := "⋯", IconButton.ariaLabel := "More",
          |  IconButton.variant := IconButton.Variant.Ghost)
          |IconButton(IconButton.icon := "⌫", IconButton.ariaLabel := "Delete",
          |  IconButton.size := IconButton.Size.Small)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          IconButton(IconButton.icon := "▶", IconButton.ariaLabel := "Play"),
          IconButton(IconButton.icon := "✎", IconButton.ariaLabel := "Edit", IconButton.variant := IconButton.Variant.Secondary),
          IconButton(IconButton.icon := "⋯", IconButton.ariaLabel := "More", IconButton.variant := IconButton.Variant.Ghost),
          IconButton(IconButton.icon := "⌫", IconButton.ariaLabel := "Delete", IconButton.size := IconButton.Size.Small)
        )
      )
    ),
    PageTemplate.propsTable(
      ("icon",      "String",  "Glyph or unicode character to render."),
      ("ariaLabel", "String",  "Required for screen readers."),
      ("variant",   "Primary|Secondary|Ghost", "Visual style."),
      ("size",      "Small|Medium",            "Size scale."),
      ("disabled",  "Boolean", "Disables interaction."),
      ("click",     "Out[Unit]", "Click event.")
    )
  )

  def closeButton(): HtmlElement = PageTemplate(
    title = "CloseButton",
    summary = "A small × button. Specialized form of IconButton."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "CloseButton",
        """CloseButton()
          |CloseButton(CloseButton.size := CloseButton.Size.Small)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          CloseButton(),
          CloseButton(CloseButton.size := CloseButton.Size.Small)
        )
      )
    ),
    PageTemplate.behavior(
      "Used in Modal, Drawer, Alert headers — anywhere you need a dismiss control.",
      "Emits a `click` event; the parent is responsible for closing/dismissing."
    ),
    PageTemplate.propsTable(
      ("size",  "Default|Small", "Size scale."),
      ("click", "Out[Unit]",     "Click event.")
    )
  )

  def downloadTrigger(): HtmlElement = PageTemplate(
    title = "DownloadTrigger",
    summary = "Anchor styled like a primary button that fires a file download."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "DownloadTrigger",
        """DownloadTrigger(
          |  DownloadTrigger.label := "Download report",
          |  DownloadTrigger.href := "data:text/plain,Example",
          |  DownloadTrigger.filename := "report.txt"
          |)""".stripMargin
      )(
        DownloadTrigger(
          DownloadTrigger.label := "Download report",
          DownloadTrigger.href := "data:text/plain,Example",
          DownloadTrigger.filename := "report.txt"
        )
      )
    ),
    PageTemplate.propsTable(
      ("label",    "String", "Button text."),
      ("href",     "String", "Resource to download (URL or data: URI)."),
      ("filename", "String", "Filename suggested to the browser.")
    )
  )
}
