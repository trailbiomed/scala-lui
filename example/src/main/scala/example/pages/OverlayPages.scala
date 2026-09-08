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
    PageTemplate.section("Chrome")(
      PageTemplate.paragraph(
        "`divided := false` collapses the rule under the header and the footer's fill and " +
          "rule. A four-line dialog otherwise arrives as three stacked bands, which reads " +
          "much heavier than the content warrants."
      ),
      PageTemplate.codedDemo(
        "Modal.divided",
        """Modal(
          |  Modal.open <--> open,
          |  Modal.title := "Rename run",
          |  Modal.divided := false,
          |  Modal.body(TextInput(TextInput.value <--> name)),
          |  Modal.footer(Button(Button.label := "Save"))
          |)""".stripMargin
      )({
        val open = Var(false)
        val name = Var("run-0417")
        div(
          Button(
            Button.label := "Open undivided dialog",
            Button.variant := Button.Variant.Secondary,
            Button.click.foreach(_ => open.set(true))
          ),
          Modal(
            Modal.open <--> open,
            Modal.title := "Rename run",
            Modal.divided := false,
            Modal.body(TextInput(TextInput.value <--> name)),
            Modal.footer(
              Button(Button.label := "Cancel", Button.variant := Button.Variant.Ghost, Button.click.foreach(_ => open.set(false))),
              Button(Button.label := "Save", Button.click.foreach(_ => open.set(false)))
            )
          )
        )
      })
    ),
    PageTemplate.section("Locked while busy")(
      PageTemplate.paragraph(
        "`busy := true` makes the dialog undismissable outright — no Escape, no backdrop, " +
          "no close button — for the span of a request it started. Once the request has " +
          "gone, closing the window does not recall it. `ConfirmDialog` wires this to its " +
          "own footer buttons."
      ),
      PageTemplate.codedDemo(
        "Modal.busy",
        """Modal(
          |  Modal.open <--> open,
          |  Modal.busy <-- saving.signal,
          |  Modal.title := "Publishing",
          |  Modal.body(span(typo.body, "Uploading the bundle…"))
          |)""".stripMargin
      )({
        val open = Var(false)
        val busy = Var(false)
        div(stack.row(spacing.md) ++ stack.wrap,
          Button(
            Button.label := "Open locked dialog",
            Button.variant := Button.Variant.Secondary,
            Button.click.foreach { _ => busy.set(true); open.set(true) }
          ),
          Modal(
            Modal.open <--> open,
            Modal.busy <-- busy.signal,
            Modal.divided := false,
            Modal.title := "Publishing",
            Modal.body(
              div(stack.col(spacing.lg),
                span(typo.body, "Escape, the backdrop and the × are all inert while this is busy."),
                div(stack.row(spacing.md) ++ css.justifyContent("flex-end"),
                  Button(
                    Button.label := "Finish",
                    Button.click.foreach { _ => busy.set(false); open.set(false) }
                  )
                )
              )
            )
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("open",        "InOut[Boolean]", "Open state."),
      ("title",       "String",         "Dialog title."),
      ("width",       "Length",         "Max width. Default 380px."),
      ("dismissible", "Boolean",        "Backdrop click, Escape and the close button. Default true."),
      ("busy",        "Boolean",        "Locks the dialog shut regardless of `dismissible`."),
      ("divided",     "Boolean",        "Header rule and footer bar. Default true."),
      ("body",        "Slot",           "Dialog content."),
      ("footer",      "Slot",           "Trailing action bar."),
      ("attr",        "Modifier*",      "Arbitrary modifiers on the dialog card.")
    )
  )

  def confirmDialog(): HtmlElement = PageTemplate(
    title = "ConfirmDialog",
    summary = "A Modal that owns its footer: cancel, confirm, a progress label, and the rule that it cannot be dismissed while the action is in flight."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "ConfirmDialog",
        """val confirming = Var(false)
          |val deleting   = Var(false)
          |ConfirmDialog(
          |  ConfirmDialog.open <--> confirming,
          |  ConfirmDialog.title := "Delete project",
          |  ConfirmDialog.message := "This removes the project and every run under it.",
          |  ConfirmDialog.confirmLabel := "Delete",
          |  ConfirmDialog.busyLabel := "Deleting…",
          |  ConfirmDialog.destructive := true,
          |  ConfirmDialog.busy <-- deleting.signal,
          |  ConfirmDialog.confirm.foreach(_ => startDelete())
          |)""".stripMargin
      )({
        val confirming = Var(false)
        val deleting = Var(false)
        val log = Var("")
        div(stack.col(spacing.md),
          Button(
            Button.label := "Delete project",
            Button.variant := Button.Variant.Danger,
            Button.click.foreach(_ => confirming.set(true))
          ),
          ConfirmDialog(
            ConfirmDialog.open <--> confirming,
            ConfirmDialog.title := "Delete project",
            ConfirmDialog.message := "This removes the project and every run under it.",
            ConfirmDialog.confirmLabel := "Delete",
            ConfirmDialog.busyLabel := "Deleting…",
            ConfirmDialog.destructive := true,
            ConfirmDialog.busy <-- deleting.signal,
            ConfirmDialog.confirm.foreach { _ =>
              deleting.set(true)
              val _ = scala.scalajs.js.timers.setTimeout(1200) {
                deleting.set(false)
                confirming.set(false)
                log.set("deleted")
              }
            },
            ConfirmDialog.dismissed.foreach(_ => log.set("dismissed"))
          ),
          span(typo.hint, child.text <-- log.signal)
        )
      })
    ),
    PageTemplate.behavior(
      "While `busy` is true the dialog is locked shut — Escape, the backdrop and the close button are all inert, and both footer buttons are disabled. Once `confirm` has fired the request has gone, and closing the window does not recall it.",
      "The confirm button reads `busyLabel` while busy, falling back to `confirmLabel` with an ellipsis.",
      "`destructive := true` renders the confirm button as `Button.Variant.Danger`.",
      "`dismissed` covers every way out — cancel, Escape, backdrop, close button — and clears `open` itself."
    ),
    PageTemplate.propsTable(
      ("open",         "InOut[Boolean]", "Open state."),
      ("title",        "String",         "Dialog title."),
      ("message",      "String",         "One line of body text."),
      ("confirmLabel", "String",         "Confirm button label. Default \"Confirm\"."),
      ("busyLabel",    "String",         "Confirm button label while busy. Defaults to `confirmLabel` + \"…\"."),
      ("cancelLabel",  "String",         "Cancel button label. Default \"Cancel\"."),
      ("busy",         "Boolean",        "The confirmed action is in flight. Locks the dialog."),
      ("destructive",  "Boolean",        "Renders the confirm button as Danger."),
      ("confirm",      "Out[Unit]",      "The confirm button was activated."),
      ("dismissed",    "Out[Unit]",      "The user backed out."),
      ("body",         "Slot",           "Body content in place of `message`.")
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
    PageTemplate.section("Disabled items")(
      PageTemplate.paragraph(
        "`Item(disabled = true)` renders the row muted and inert: it emits nothing, and " +
          "the arrow keys skip over it rather than parking focus somewhere Enter does " +
          "nothing. Use it for a row that has to stay visible to explain itself."
      ),
      PageTemplate.codedDemo(
        "Menu.Item(disabled = true)",
        """Menu(
          |  Menu.items := Seq(
          |    Menu.Item("add", "Add tag", "＋"),
          |    Menu.Item("none", "No tags yet", disabled = true)
          |  ),
          |  Menu.trigger(Button(Button.label := "Tags"))
          |)""".stripMargin
      )({
        val last = Var("")
        div(stack.col(spacing.sm),
          Menu(
            Menu.items := Seq(
              Menu.Item("add", "Add tag", "＋"),
              Menu.Item("none", "No tags yet", disabled = true)
            ),
            Menu.select --> last.writer,
            Menu.trigger(Button(Button.label := "Tags", Button.variant := Button.Variant.Secondary))
          ),
          span(typo.hint, child.text <-- last.signal.map(s => if (s.isEmpty) "" else s"selected: $s"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("items",   "Seq[Menu.Item]", "Item(key, label, icon, danger, disabled) entries."),
      ("select",  "Out[String]",    "Emits the selected item's key. Disabled items emit nothing."),
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

  def fullscreenOverlay(): HtmlElement = PageTemplate(
    title = "FullscreenOverlay",
    summary = "Viewport-filling surface with no backdrop or chrome. Slideshows, presentation modes, kiosk views."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "FullscreenOverlay",
        """val open = Var(false)
          |Button(Button.label := "Enter fullscreen",
          |  Button.click.foreach(_ => open.set(true)))
          |FullscreenOverlay(
          |  FullscreenOverlay.open <--> open,
          |  FullscreenOverlay.body(
          |    div(stack.centerAll ++ stack.grow,
          |      Heading(1)("Now presenting."),
          |      Button(Button.label := "Exit",
          |        Button.click.foreach(_ => open.set(false)))
          |    )
          |  )
          |)""".stripMargin
      )({
        val open = Var(false)
        div(
          Button(Button.label := "Enter fullscreen", Button.click.foreach(_ => open.set(true))),
          FullscreenOverlay(
            FullscreenOverlay.open <--> open,
            FullscreenOverlay.body(
              div(
                stack.centerAll ++ stack.grow ++ css.padding(spacing.xxl),
                div(
                  stack.col(spacing.lg) ++ css.alignItems("center"),
                  Heading(1)("Now presenting."),
                  span(typo.muted, "Press Escape or the button below to exit."),
                  Button(Button.label := "Exit", Button.click.foreach(_ => open.set(false)))
                )
              )
            )
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("open",      "InOut[Boolean]", "Open state."),
      ("zIndex",    "Int",            "Stacking order. Default 100."),
      ("trapFocus", "Boolean",        "Trap Tab within the overlay. Default true."),
      ("close",     "Out[Unit]",      "Sink invoked on Escape or external dismiss."),
      ("body",      "Slot",           "Content that fills the viewport.")
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
