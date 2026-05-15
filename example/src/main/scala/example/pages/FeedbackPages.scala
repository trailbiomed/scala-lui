package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object FeedbackPages {

  def alert(): HtmlElement = PageTemplate(
    title = "Alert",
    summary = "Inline message with a title and rich body. Four severity variants."
  )(
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Alert.variant",
        """Alert(
          |  Alert.title := "Heads up",
          |  Alert.variant := Alert.Variant.Info,
          |  Alert.body(span("This run will use a non-default reference."))
          |)""".stripMargin
      )(
        div(stack.col(spacing.md),
          Alert(Alert.title := "Heads up",      Alert.variant := Alert.Variant.Info,    Alert.body(span("This run will use a non-default reference."))),
          Alert(Alert.title := "Saved",         Alert.variant := Alert.Variant.Success, Alert.body(span("Reference imported and indexed."))),
          Alert(Alert.title := "Quota nearing", Alert.variant := Alert.Variant.Warning, Alert.dismissible := true, Alert.body(span("You're at 87% of monthly compute."))),
          Alert(Alert.title := "Build failed",  Alert.variant := Alert.Variant.Danger,  Alert.dismissible := true, Alert.body(span("Cyclic dependency between modules A and B.")))
        )
      )
    ),
    PageTemplate.behavior(
      "Set `dismissible := true` to add a close button; clicking it hides the alert without removing it from the DOM."
    ),
    PageTemplate.propsTable(
      ("title",       "String",                            "Alert title."),
      ("variant",     "Info|Success|Warning|Danger",       "Severity. Picks the color treatment."),
      ("dismissible", "Boolean",                           "Show a close button."),
      ("body",        "Slot",                              "Free-form body content.")
    )
  )

  def emptyState(): HtmlElement = PageTemplate(
    title = "EmptyState",
    summary = "Centered placeholder for empty lists, with an icon, title, description, and optional action."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "EmptyState",
        """EmptyState(
          |  EmptyState.icon := "∅",
          |  EmptyState.title := "No projects yet",
          |  EmptyState.description := "Create your first project to see it listed here.",
          |  EmptyState.action(Button(Button.label := "+ New project"))
          |)""".stripMargin
      )(
        EmptyState(
          EmptyState.icon := "∅",
          EmptyState.title := "No projects yet",
          EmptyState.description := "Create your first project to see it listed here.",
          EmptyState.action(Button(Button.label := "+ New project"))
        )
      )
    ),
    PageTemplate.propsTable(
      ("icon",        "String", "Glyph or unicode character shown above the title."),
      ("title",       "String", "Title line."),
      ("description", "String", "Body text."),
      ("action",      "Slot",   "Optional CTA (usually a Button).")
    )
  )

  def spinner(): HtmlElement = PageTemplate(
    title = "Spinner",
    summary = "Indeterminate progress indicator. Animated via JS setInterval (no @keyframes)."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Spinner(size)",
        """Spinner(Spinner.size := Length.px(14))
          |Spinner(Spinner.size := Length.px(20))
          |Spinner(Spinner.size := Length.px(28))
          |Spinner(Spinner.size := Length.px(40))""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          Spinner(Spinner.size := Length.px(14)),
          Spinner(Spinner.size := Length.px(20)),
          Spinner(Spinner.size := Length.px(28)),
          Spinner(Spinner.size := Length.px(40))
        )
      )
    ),
    PageTemplate.propsTable(
      ("size", "Length", "Width and height of the spinner. Default 16px.")
    )
  )

  def progressBar(): HtmlElement = PageTemplate(
    title = "ProgressBar",
    summary = "Horizontal determinate progress bar. Supports an indeterminate mode."
  )(
    PageTemplate.section("Determinate")(
      PageTemplate.codedDemo(
        "ProgressBar.value",
        """ProgressBar(ProgressBar.value := 0.30)
          |ProgressBar(ProgressBar.value := 0.65,
          |  ProgressBar.variant := ProgressBar.Variant.Success)
          |ProgressBar(ProgressBar.value := 0.40,
          |  ProgressBar.variant := ProgressBar.Variant.Warning)""".stripMargin
      )(
        div(stack.col(spacing.md),
          ProgressBar(ProgressBar.value := 0.30),
          ProgressBar(ProgressBar.value := 0.65, ProgressBar.variant := ProgressBar.Variant.Success),
          ProgressBar(ProgressBar.value := 0.40, ProgressBar.variant := ProgressBar.Variant.Warning)
        )
      )
    ),
    PageTemplate.section("Indeterminate")(
      PageTemplate.codedDemo(
        "ProgressBar.indeterminate",
        "ProgressBar(ProgressBar.indeterminate := true)"
      )(
        ProgressBar(ProgressBar.indeterminate := true)
      )
    ),
    PageTemplate.propsTable(
      ("value",         "Double",                       "Progress between 0 and 1."),
      ("variant",       "Brand|Success|Warning|Danger", "Color treatment. Default Brand."),
      ("indeterminate", "Boolean",                      "Animated loop, ignores value.")
    )
  )

  def progressCircle(): HtmlElement = PageTemplate(
    title = "ProgressCircle",
    summary = "Donut-shaped determinate progress indicator with an optional center label."
  )(
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "ProgressCircle",
        """ProgressCircle(
          |  ProgressCircle.value := 0.42,
          |  ProgressCircle.size := Length.px(48),
          |  ProgressCircle.thickness := Length.px(6),
          |  ProgressCircle.showLabel := true
          |)""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          ProgressCircle(ProgressCircle.value := 0.42, ProgressCircle.size := Length.px(48), ProgressCircle.thickness := Length.px(6), ProgressCircle.showLabel := true),
          ProgressCircle(ProgressCircle.value := 0.72, ProgressCircle.variant := ProgressCircle.Variant.Success, ProgressCircle.size := Length.px(48), ProgressCircle.thickness := Length.px(6), ProgressCircle.showLabel := true),
          ProgressCircle(ProgressCircle.value := 0.15, ProgressCircle.variant := ProgressCircle.Variant.Danger,  ProgressCircle.size := Length.px(48), ProgressCircle.thickness := Length.px(6), ProgressCircle.showLabel := true)
        )
      )
    ),
    PageTemplate.propsTable(
      ("value",     "Double",                       "Progress between 0 and 1."),
      ("size",      "Length",                       "Outer diameter."),
      ("thickness", "Length",                       "Stroke thickness of the ring."),
      ("variant",   "Brand|Success|Warning|Danger", "Color treatment."),
      ("showLabel", "Boolean",                      "Show a percent label in the center.")
    )
  )

  def skeleton(): HtmlElement = PageTemplate(
    title = "Skeleton",
    summary = "Placeholder rectangle. Use to indicate loading content."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Skeleton",
        """Skeleton(Skeleton.height := Length.px(14), Skeleton.width := Length.pct(60))
          |Skeleton(Skeleton.height := Length.px(10))
          |Skeleton(Skeleton.height := Length.px(10), Skeleton.width := Length.pct(82))""".stripMargin
      )(
        div(stack.col(spacing.md) ++ css.width(Length.pct(100)),
          Skeleton(Skeleton.height := Length.px(14), Skeleton.width := Length.pct(60)),
          Skeleton(Skeleton.height := Length.px(10)),
          Skeleton(Skeleton.height := Length.px(10), Skeleton.width := Length.pct(82))
        )
      )
    ),
    PageTemplate.propsTable(
      ("height", "Length", "Block height."),
      ("width",  "Length", "Block width. Default 100%.")
    )
  )

  def toast(): HtmlElement = PageTemplate(
    title = "Toast",
    summary = "Globally-mounted transient message stack."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Toast.show(msg)",
        """// once, in your app root:
          |Toast()
          |
          |// anywhere:
          |Toast.show("Saved.")
          |Toast.show("Imported 12 files.")""".stripMargin
      )({
        val count = Var(0)
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Button(Button.label := "Show toast",
            Button.click.foreach { _ => count.update(_ + 1); Toast.show(s"Toast #${count.now()}") }),
          span(typo.muted, "Toast() is mounted once in Main.scala.")
        )
      })
    ),
    PageTemplate.behavior(
      "Mount Toast() once at the app root. Anywhere else, call Toast.show(msg).",
      "Each toast auto-dismisses after a few seconds."
    ),
    PageTemplate.propsTable(
      ("Toast()",       "HtmlElement", "Mount the toast container. Once per app."),
      ("Toast.show(s)", "String => Unit", "Push a new toast onto the stack.")
    )
  )
}
