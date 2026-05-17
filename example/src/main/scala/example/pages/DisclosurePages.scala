package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object DisclosurePages {

  def accordion(): HtmlElement = PageTemplate(
    title = "Accordion",
    summary = "Expand/collapse section with a title and a one-line summary."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Accordion",
        """Accordion(
          |  Accordion.title := "Pre-processing",
          |  Accordion.summary := "scale, log, select",
          |  Accordion.body(
          |    Checkbox(Checkbox.label := "Log-transform"),
          |    Checkbox(Checkbox.label := "Scale by gene")
          |  )
          |)""".stripMargin
      )(
        div(stack.col(spacing.md),
          Accordion(
            Accordion.title := "Pre-processing",
            Accordion.summary := "scale, log, select",
            Accordion.body(
              Checkbox(Checkbox.label := "Log-transform"),
              Checkbox(Checkbox.label := "Scale by gene"),
              Checkbox(Checkbox.label := "Drop low-variance genes")
            )
          ),
          Accordion(
            Accordion.title := "Compute",
            Accordion.summary := "K range, replicates",
            Accordion.body(
              span(typo.muted, "K range and replicate count go here.")
            )
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("title",   "String",         "Section title."),
      ("summary", "String",         "Inline summary shown next to the title."),
      ("open",    "InOut[Boolean]", "Two-way binding for the open state."),
      ("body",    "Slot",           "Children rendered when open.")
    )
  )

  def collapsible(): HtmlElement = PageTemplate(
    title = "Collapsible",
    summary = "Open/close container without a header. Pair with your own toggle."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Collapsible",
        """val open = Var(false)
          |Button(
          |  Button.label <-- open.signal.map(o => if (o) "Hide" else "Show"),
          |  Button.click.foreach(_ => open.update(b => !b))
          |)
          |Collapsible(
          |  Collapsible.open <--> open,
          |  Collapsible.body(span("Hidden details revealed."))
          |)""".stripMargin
      )({
        val open = Var(false)
        div(
          stack.col(spacing.md),
          Button(
            Button.label <-- open.signal.map(o => if (o) "Hide" else "Show"),
            Button.variant := Button.Variant.Secondary,
            Button.size := Button.Size.Small,
            Button.click.foreach(_ => open.update(b => !b))
          ),
          Collapsible(
            Collapsible.open <--> open,
            Collapsible.body(
              div(themed(t => css.padding(spacing.lg) ++ css.background(t.surfaceDim) ++ css.borderRadius(radius.md)),
                span(typo.body, "Hidden details revealed."))
            )
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("open", "InOut[Boolean]", "Two-way binding for the open state."),
      ("body", "Slot",           "Children rendered when open.")
    )
  )

  def tabs(): HtmlElement = PageTemplate(
    title = "Tabs",
    summary = "Bar of tab buttons with an active state."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Tabs",
        """val tab = Var("modules")
          |Tabs(
          |  Tabs.tabs := Seq(
          |    "modules"  -> "Modules",
          |    "topgenes" -> "Top genes",
          |    "cluster"  -> "Clustering"
          |  ),
          |  Tabs.active <--> tab
          |)""".stripMargin
      )({
        val v = Var("modules")
        div(stack.col(spacing.lg),
          Tabs(
            Tabs.tabs := Seq("modules" -> "Modules", "topgenes" -> "Top genes", "cluster" -> "Clustering"),
            Tabs.active <--> v
          ),
          div(themed(t => css.padding(spacing.lg) ++ css.background(t.surfaceDim) ++ css.borderRadius(radius.md)),
            span(typo.muted, child.text <-- v.signal.map(s => s"active: $s")))
        )
      })
    ),
    PageTemplate.propsTable(
      ("tabs",   "Seq[(String,String)]", "(key, label) pairs."),
      ("active", "InOut[String]",        "Active tab key.")
    )
  )

  def breadcrumb(): HtmlElement = PageTemplate(
    title = "Breadcrumb",
    summary = "Trail of links showing the current view's location in a hierarchy."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Breadcrumb",
        """Breadcrumb(
          |  Breadcrumb.items := Seq(
          |    "home" -> "Home",
          |    "wb"   -> "Workbench",
          |    "ref"  -> "References",
          |    "demo" -> "demo_run_2026"
          |  ),
          |  Breadcrumb.select --> selected.writer
          |)""".stripMargin
      )({
        val last = Var("")
        div(stack.col(spacing.sm),
          Breadcrumb(
            Breadcrumb.items := Seq("home" -> "Home", "wb" -> "Workbench", "ref" -> "References", "demo" -> "demo_run_2026"),
            Breadcrumb.select --> last.writer
          ),
          span(typo.hint, child.text <-- last.signal.map(c => if (c.isEmpty) "" else s"last clicked: $c"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("items",  "Seq[(String,String)]", "(key, label) pairs, ordered root-first."),
      ("select", "Out[String]",          "Emits the key of the clicked crumb.")
    )
  )

  def pagination(): HtmlElement = PageTemplate(
    title = "Pagination",
    summary = "Numbered page navigator with previous/next arrows."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Pagination",
        """val page = Var(3)
          |Pagination(
          |  Pagination.page <--> page,
          |  Pagination.totalPages := 12,
          |  Pagination.siblings := 1
          |)""".stripMargin
      )({
        val page = Var(3)
        div(stack.col(spacing.sm),
          Pagination(Pagination.page <--> page, Pagination.totalPages := 12, Pagination.siblings := 1),
          span(typo.hint, child.text <-- page.signal.map(p => s"on page $p"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("page",       "InOut[Int]", "Active page (1-based)."),
      ("totalPages", "Int",        "Total page count."),
      ("siblings",   "Int",        "How many pages to show on each side of the current one.")
    )
  )

  def tabPanel(): HtmlElement = PageTemplate(
    title = "TabPanel",
    summary = "Tabs strip + content bodies in one piece, with height-stable layout so clicks don't make the page jump."
  )(
    PageTemplate.section("Stable layout (default)")(
      PageTemplate.codedDemo(
        "TabPanel",
        """val active = Var("claims")
          |TabPanel(
          |  TabPanel.active <--> active,
          |  TabPanel.panel("claims",         "Claims (3)",         claimsBody),
          |  TabPanel.panel("contradictions", "Contradictions (1)", contraBody),
          |  TabPanel.panel("raw",            "Raw JSON",           rawBody),
          |)""".stripMargin
      )({
        val active = Var("claims")
        TabPanel(
          TabPanel.active <--> active,
          TabPanel.panel(
            "claims",
            "Claims (3)",
            div(stack.col(spacing.sm),
              span(typo.label, "Claim 1"), span(typo.body, "Short body"),
              span(typo.label, "Claim 2"), span(typo.body, "Short body"),
              span(typo.label, "Claim 3"), span(typo.body, "Short body")
            )
          ),
          TabPanel.panel(
            "contradictions",
            "Contradictions (1)",
            span(typo.muted, "One contradiction.")
          ),
          TabPanel.panel(
            "raw",
            "Raw JSON",
            div(stack.col(spacing.md),
              (1 to 12).map(i => span(typo.muted, s"line $i — placeholder content"))
            )
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("active", "InOut[String]", "Active panel key. Defaults to first declared."),
      ("variant", "In[Tabs.Variant]", "Strip variant: Underlined (default) or Pills."),
      ("layout", "In[Layout]", "Stable (default; grid+visibility) or Swap (child <--)."),
      ("panel(key, label, content)", "Mod", "Declare one panel; first declared is the default active.")
    )
  )

  def steps(): HtmlElement = PageTemplate(
    title = "Steps",
    summary = "Numbered horizontal progress stepper for multi-step flows."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Steps",
        """val step = Var(1)
          |Steps(
          |  Steps.steps := Seq("Reference", "Pre-process", "Compute K", "Review"),
          |  Steps.current <-- step.signal
          |)""".stripMargin
      )({
        val step = Var(1)
        div(stack.col(spacing.lg),
          Steps(Steps.steps := Seq("Reference", "Pre-process", "Compute K", "Review"), Steps.current <-- step.signal),
          div(stack.row(spacing.md),
            Button(Button.label := "← Back", Button.variant := Button.Variant.Secondary, Button.size := Button.Size.Small, Button.click.foreach(_ => step.update(s => math.max(0, s - 1)))),
            Button(Button.label := "Next →", Button.size := Button.Size.Small, Button.click.foreach(_ => step.update(s => math.min(3, s + 1))))
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("steps",   "Seq[String]", "Ordered step labels."),
      ("current", "In[Int]",     "Index of the active step (0-based).")
    )
  )
}
