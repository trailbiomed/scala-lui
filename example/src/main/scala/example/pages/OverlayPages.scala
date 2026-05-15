package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object OverlayPages {

  def modal(): HtmlElement = PageTemplate(
    title = "Modal",
    summary = "Centered dialog over a backdrop. Click outside closes it."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Modal",
        """val open = Var(false)
          |Button(Button.label := "Open dialog",
          |  Button.click.foreach(_ => open.set(true)))
          |Modal(
          |  Modal.open <--> open,
          |  Modal.title := "Confirm action",
          |  Modal.body(
          |    p(typo.body, "Archive this analysis? You can restore it later."),
          |    Button(Button.label := "Archive",
          |      Button.click.foreach(_ => open.set(false)))
          |  )
          |)""".stripMargin
      )({
        val open = Var(false)
        div(
          Button(Button.label := "Open dialog", Button.click.foreach(_ => open.set(true))),
          Modal(
            Modal.open <--> open,
            Modal.title := "Confirm action",
            Modal.body(
              p(typo.body ++ css.margin(Length.px(0)), "Archive this analysis? You can restore it later."),
              div(stack.row(spacing.md) ++ css.justifyContent("flex-end"),
                Button(Button.label := "Cancel",  Button.variant := Button.Variant.Ghost,   Button.click.foreach(_ => open.set(false))),
                Button(Button.label := "Archive", Button.variant := Button.Variant.Primary, Button.click.foreach(_ => open.set(false)))
              )
            )
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("open",  "InOut[Boolean]", "Open state."),
      ("title", "String",         "Dialog title."),
      ("width", "Length",         "Max width. Default 380px."),
      ("body",  "Slot",           "Dialog content.")
    )
  )

  def drawer(): HtmlElement = PageTemplate(
    title = "Drawer",
    summary = "Side panel overlay. Slides from left or right."
  )(
    PageTemplate.section("Right (default)")(
      PageTemplate.codedDemo(
        "Drawer",
        """val open = Var(false)
          |Drawer(
          |  Drawer.open <--> open,
          |  Drawer.title := "Run details",
          |  Drawer.body(
          |    DataList(DataList.items := Seq(
          |      "Run" -> "demo_run_2026",
          |      "K"   -> "8"
          |    ))
          |  )
          |)""".stripMargin
      )({
        val open = Var(false)
        div(
          Button(Button.label := "Open drawer", Button.click.foreach(_ => open.set(true))),
          Drawer(
            Drawer.open <--> open,
            Drawer.title := "Run details",
            Drawer.body(
              span(typo.muted, "Detailed view appears here."),
              DataList(
                DataList.items := Seq("Run" -> "demo_run_2026", "K" -> "8", "Owner" -> "John Doe")
              )
            )
          )
        )
      })
    ),
    PageTemplate.section("Left")(
      PageTemplate.codedDemo(
        "Drawer.side := Left",
        """Drawer(
          |  Drawer.open <--> open,
          |  Drawer.side := Drawer.Side.Left,
          |  Drawer.title := "Filters",
          |  Drawer.body(span(typo.muted, "Filter controls go here."))
          |)""".stripMargin
      )({
        val open = Var(false)
        div(
          Button(Button.label := "Open left drawer", Button.click.foreach(_ => open.set(true))),
          Drawer(
            Drawer.open <--> open,
            Drawer.side := Drawer.Side.Left,
            Drawer.title := "Filters",
            Drawer.body(span(typo.muted, "Filter controls go here."))
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("open",  "InOut[Boolean]", "Open state."),
      ("side",  "Left|Right",     "Anchor edge."),
      ("title", "String",         "Drawer title."),
      ("width", "Length",         "Drawer width. Default 360px."),
      ("body",  "Slot",           "Drawer content.")
    )
  )

  def tooltip(): HtmlElement = PageTemplate(
    title = "Tooltip",
    summary = "Hover-only popover for short helper text. No-op on touch."
  )(
    PageTemplate.section("Placements")(
      PageTemplate.codedDemo(
        "Tooltip.placement",
        """Tooltip(
          |  Tooltip.label := "Run analysis",
          |  Tooltip.placement := Tooltip.Placement.Top,
          |  Tooltip.trigger(IconButton(
          |    IconButton.icon := "▶",
          |    IconButton.ariaLabel := "Run"))
          |)""".stripMargin
      )(
        div(stack.row(spacing.xxl),
          Tooltip(Tooltip.label := "Top",    Tooltip.placement := Tooltip.Placement.Top,    Tooltip.trigger(IconButton(IconButton.icon := "▶", IconButton.ariaLabel := "Run"))),
          Tooltip(Tooltip.label := "Right",  Tooltip.placement := Tooltip.Placement.Right,  Tooltip.trigger(IconButton(IconButton.icon := "✎", IconButton.ariaLabel := "Edit"))),
          Tooltip(Tooltip.label := "Bottom", Tooltip.placement := Tooltip.Placement.Bottom, Tooltip.trigger(IconButton(IconButton.icon := "⌫", IconButton.ariaLabel := "Delete"))),
          Tooltip(Tooltip.label := "Left",   Tooltip.placement := Tooltip.Placement.Left,   Tooltip.trigger(IconButton(IconButton.icon := "⋯", IconButton.ariaLabel := "More")))
        )
      )
    ),
    PageTemplate.behavior(
      "Use this for short, ephemeral helper text. For rich content, use HoverCard.",
      "Touch devices don't fire hover events, so Tooltip is invisible on touch. Use ToggleTip there."
    ),
    PageTemplate.propsTable(
      ("label",     "String",                "Tip text."),
      ("placement", "Top|Right|Bottom|Left", "Where the tip appears relative to the trigger."),
      ("trigger",   "Slot",                  "The element the tip describes.")
    )
  )

  def popover(): HtmlElement = PageTemplate(
    title = "Popover",
    summary = "Click-toggled overlay panel. The building block for Menu, HoverCard, and ToggleTip."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Popover",
        """Popover(
          |  Popover.placement := Popover.Placement.Bottom,
          |  Popover.trigger(Button(
          |    Button.label := "Open popover",
          |    Button.variant := Button.Variant.Secondary)),
          |  Popover.body(
          |    span(typo.label, "Quick actions"),
          |    span(typo.muted, "Click outside to close.")
          |  )
          |)""".stripMargin
      )(
        Popover(
          Popover.placement := Popover.Placement.Bottom,
          Popover.trigger(Button(Button.label := "Open popover", Button.variant := Button.Variant.Secondary)),
          Popover.body(
            div(stack.col(spacing.sm) ++ css.raw("min-width", "220px"),
              span(typo.label, "Quick actions"),
              span(typo.muted, "Click outside to close.")
            )
          )
        )
      )
    ),
    PageTemplate.behavior(
      "Click anywhere outside the popover closes it.",
      "Re-clicking the trigger toggles the state."
    ),
    PageTemplate.propsTable(
      ("open",      "InOut[Boolean]",        "Open state."),
      ("placement", "Top|Right|Bottom|Left", "Where the panel appears relative to the trigger."),
      ("trigger",   "Slot",                  "The clickable trigger element."),
      ("body",      "Slot",                  "The panel content.")
    )
  )

  def menu(): HtmlElement = PageTemplate(
    title = "Menu",
    summary = "Click-toggled action menu. Built on Popover."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Menu",
        """Menu(
          |  Menu.items := Seq(
          |    Menu.Item("rename",   "Rename",    "✎"),
          |    Menu.Item("share",    "Share",     "↗"),
          |    Menu.Item("archive",  "Archive",   "⌫", danger = true)
          |  ),
          |  Menu.select --> selected.writer,
          |  Menu.trigger(IconButton(
          |    IconButton.icon := "⋯",
          |    IconButton.ariaLabel := "Actions"))
          |)""".stripMargin
      )({
        val last = Var("")
        div(stack.col(spacing.sm),
          Menu(
            Menu.items := Seq(
              Menu.Item("rename",   "Rename",      "✎"),
              Menu.Item("share",    "Share",       "↗"),
              Menu.Item("duplicate","Duplicate",   "⎘"),
              Menu.Item("archive",  "Archive",     "⌫", danger = true)
            ),
            Menu.select --> last.writer,
            Menu.trigger(IconButton(IconButton.icon := "⋯", IconButton.ariaLabel := "Actions"))
          ),
          span(typo.hint, child.text <-- last.signal.map(s => if (s.isEmpty) "" else s"selected: $s"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("items",   "Seq[Menu.Item]", "Item(key, label, icon, danger) entries."),
      ("select",  "Out[String]",    "Emits the selected item's key."),
      ("trigger", "Slot",           "The clickable trigger element.")
    )
  )

  def hoverCard(): HtmlElement = PageTemplate(
    title = "HoverCard",
    summary = "Rich-content popover that opens on hover. Tooltip with structure."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "HoverCard",
        """HoverCard(
          |  HoverCard.trigger(span(typo.label, "Hover me")),
          |  HoverCard.body(
          |    div(stack.col(spacing.xs),
          |      span(typo.label, "John Doe"),
          |      span(typo.muted, "john@example.com"),
          |      span(typo.hint, "Last active 2 minutes ago")
          |    )
          |  )
          |)""".stripMargin
      )(
        HoverCard(
          HoverCard.trigger(span(typo.label ++ css.cursor("default"), "Hover me")),
          HoverCard.body(
            div(stack.col(spacing.xs),
              span(typo.label, "John Doe"),
              span(typo.muted, "john@example.com"),
              span(typo.hint, "Last active 2 minutes ago")
            )
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("placement", "Top|Right|Bottom|Left", "Anchor position."),
      ("trigger",   "Slot",                  "The element to hover."),
      ("body",      "Slot",                  "Card contents.")
    )
  )

  def toggleTip(): HtmlElement = PageTemplate(
    title = "ToggleTip",
    summary = "Click-to-toggle small tip popover. Better for touch than Tooltip."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "ToggleTip",
        """ToggleTip(
          |  ToggleTip.label := "Measured in wall-clock seconds across all workers.",
          |  ToggleTip.trigger(IconButton(
          |    IconButton.icon := "?",
          |    IconButton.ariaLabel := "What is this?",
          |    IconButton.size := IconButton.Size.Small,
          |    IconButton.variant := IconButton.Variant.Ghost))
          |)""".stripMargin
      )(
        div(stack.row(spacing.sm) ++ css.alignItems("center"),
          span(typo.body, "Compute time"),
          ToggleTip(
            ToggleTip.label := "Measured in wall-clock seconds across all workers.",
            ToggleTip.trigger(IconButton(IconButton.icon := "?", IconButton.ariaLabel := "What is this?", IconButton.size := IconButton.Size.Small, IconButton.variant := IconButton.Variant.Ghost))
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("label",     "String",                "Tip text."),
      ("placement", "Top|Right|Bottom|Left", "Anchor position."),
      ("trigger",   "Slot",                  "The clickable trigger.")
    )
  )
}
