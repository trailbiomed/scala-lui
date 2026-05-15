package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object DataDisplayPages {

  def avatar(): HtmlElement = PageTemplate(
    title = "Avatar",
    summary = "User-initials chip with size and shape variants."
  )(
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Avatar.size",
        """Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Xs)
          |Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Sm)
          |Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Md)
          |Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Lg)
          |Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Xl)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ css.alignItems("center"),
          Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Xs),
          Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Sm),
          Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Md),
          Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Lg),
          Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Xl)
        )
      )
    ),
    PageTemplate.section("Square")(
      PageTemplate.codedDemo(
        "Avatar.shape := Square",
        """Avatar(
          |  Avatar.name := "John Doe",
          |  Avatar.shape := Avatar.Shape.Square,
          |  Avatar.size := Avatar.Size.Lg
          |)""".stripMargin
      )(
        Avatar(Avatar.name := "John Doe", Avatar.shape := Avatar.Shape.Square, Avatar.size := Avatar.Size.Lg)
      )
    ),
    PageTemplate.behavior(
      "Initials come from the first letter of each whitespace-separated word.",
      "Pass Avatar.src := \"https://...\" to use an image instead of initials."
    ),
    PageTemplate.propsTable(
      ("name",  "String",      "Display name. Initials are derived from it."),
      ("src",   "String",      "Optional image URL. Falls back to initials on error."),
      ("size",  "Xs|Sm|Md|Lg|Xl", "Size scale."),
      ("shape", "Circle|Square",  "Avatar shape. Default Circle.")
    )
  )

  def badge(): HtmlElement = PageTemplate(
    title = "Badge",
    summary = "Compact label for counts and short statuses. The most generic of Tag, StatusBadge, Badge."
  )(
    PageTemplate.section("When to use Badge")(
      PageTemplate.paragraph(
        "Use Badge for counts, single-word flags, and small dot indicators with a full semantic palette " +
          "(Brand, Success, Warning, Danger, Info, Neutral). For category chips, use Tag. " +
          "For long-running job state with optional pulse, use StatusBadge."
      )
    ),
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Badge.variant",
        """Badge(Badge.label := "12",  Badge.variant := Badge.Variant.Brand)
          |Badge(Badge.label := "new", Badge.variant := Badge.Variant.Success)
          |Badge(Badge.label := "!",   Badge.variant := Badge.Variant.Warning)
          |Badge(Badge.label := "3",   Badge.variant := Badge.Variant.Danger)
          |Badge(Badge.label := "99+", Badge.variant := Badge.Variant.Neutral)""".stripMargin
      )(
        div(stack.row(spacing.sm) ++ stack.wrap,
          Badge(Badge.label := "12",  Badge.variant := Badge.Variant.Brand),
          Badge(Badge.label := "new", Badge.variant := Badge.Variant.Success),
          Badge(Badge.label := "!",   Badge.variant := Badge.Variant.Warning),
          Badge(Badge.label := "3",   Badge.variant := Badge.Variant.Danger),
          Badge(Badge.label := "i",   Badge.variant := Badge.Variant.Info),
          Badge(Badge.label := "99+", Badge.variant := Badge.Variant.Neutral)
        )
      )
    ),
    PageTemplate.section("Dot")(
      PageTemplate.codedDemo(
        "Badge.dot := true",
        """Badge(Badge.dot := true, Badge.variant := Badge.Variant.Success)""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ stack.wrap,
          span(stack.row(spacing.xs), Badge(Badge.dot := true, Badge.variant := Badge.Variant.Success), span(typo.body, "online")),
          span(stack.row(spacing.xs), Badge(Badge.dot := true, Badge.variant := Badge.Variant.Warning), span(typo.body, "degraded")),
          span(stack.row(spacing.xs), Badge(Badge.dot := true, Badge.variant := Badge.Variant.Danger), span(typo.body, "down"))
        )
      )
    ),
    PageTemplate.propsTable(
      ("label",   "String",                                       "Badge text. Ignored when dot is true."),
      ("variant", "Brand|Success|Warning|Danger|Info|Neutral",    "Color treatment."),
      ("dot",     "Boolean",                                      "Render a dot instead of a labeled pill.")
    )
  )

  def card(): HtmlElement = PageTemplate(
    title = "Card",
    summary = "Themed panel with padding. Set `interactive := true` for a hover/click variant."
  )(
    PageTemplate.section("Static")(
      PageTemplate.codedDemo(
        "Card",
        """Card(
          |  Card.children(
          |    span(typo.label, "Run #482"),
          |    span(typo.muted, "Started 2 minutes ago by John Doe.")
          |  )
          |)""".stripMargin
      )(
        Card(
          Card.children(
            span(typo.label, "Run #482"),
            span(typo.muted, "Started 2 minutes ago by John Doe.")
          )
        )
      )
    ),
    PageTemplate.section("Interactive")(
      PageTemplate.codedDemo(
        "Card.interactive",
        """Card(
          |  Card.interactive := true,
          |  Card.click.foreach(_ => println("clicked")),
          |  Card.children(
          |    span(typo.label, "Click me"),
          |    span(typo.muted, "I light up on hover.")
          |  )
          |)""".stripMargin
      )(
        Card(
          Card.interactive := true,
          Card.children(
            span(typo.label, "Click me"),
            span(typo.muted, "I light up on hover.")
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("interactive", "Boolean",   "Enables hover border + click cursor."),
      ("padding",     "Length",    "Inner padding. Default spacing.xl."),
      ("click",       "Out[Unit]", "Click event (only fires when interactive is true)."),
      ("children",    "Slot",      "Card content.")
    )
  )

  def tag(): HtmlElement = PageTemplate(
    title = "Tag",
    summary = "Inline chip for categorization. The descriptor of what a thing is."
  )(
    PageTemplate.section("When to use Tag")(
      PageTemplate.paragraph(
        "Use Tag to categorize content. Filter chips, content tags, labels like \"frontend\", \"scala\", \"wip\". " +
          "Tags can be removable (with an × button). For job state that changes over time, use StatusBadge. " +
          "For counts or single-word flags, use Badge."
      )
    ),
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Tag",
        """Tag(Tag.label := "interesting", Tag.variant := Tag.Variant.Interesting)
          |Tag(Tag.label := "warning",     Tag.variant := Tag.Variant.Warning)
          |Tag(Tag.label := "neutral",     Tag.variant := Tag.Variant.Neutral)
          |Tag(Tag.label := "removable",   Tag.variant := Tag.Variant.Neutral,
          |    Tag.removable := true)""".stripMargin
      )(
        div(stack.row(spacing.sm) ++ stack.wrap,
          Tag(Tag.label := "interesting", Tag.variant := Tag.Variant.Interesting),
          Tag(Tag.label := "warning",     Tag.variant := Tag.Variant.Warning),
          Tag(Tag.label := "neutral",     Tag.variant := Tag.Variant.Neutral),
          Tag(Tag.label := "removable",   Tag.variant := Tag.Variant.Neutral, Tag.removable := true)
        )
      )
    ),
    PageTemplate.propsTable(
      ("label",     "String",                          "Tag text."),
      ("variant",   "Interesting|Warning|Neutral",     "Color treatment."),
      ("removable", "Boolean",                         "Show an × button on the right."),
      ("remove",    "Out[Unit]",                       "Emitted when the × is clicked.")
    )
  )

  def statusBadge(): HtmlElement = PageTemplate(
    title = "StatusBadge",
    summary = "A pill showing the state of a long-running job: running, queued, success, warning."
  )(
    PageTemplate.section("When to use StatusBadge")(
      PageTemplate.paragraph(
        "Use StatusBadge to show what a thing is doing right now: run status, deployment state, build progress. " +
          "The variants are lifecycle states (Running, Queued, Success, Warning) and the badge can pulse for in-flight states. " +
          "For descriptive labels (\"frontend\", \"wip\"), use Tag. For counts or one-glance flags, use Badge."
      )
    ),
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "StatusBadge",
        """StatusBadge(
          |  StatusBadge.label := "Running",
          |  StatusBadge.variant := StatusBadge.Variant.Running,
          |  StatusBadge.pulsing := true
          |)
          |StatusBadge(StatusBadge.label := "Queued",
          |  StatusBadge.variant := StatusBadge.Variant.Queued)""".stripMargin
      )(
        div(stack.row(spacing.md) ++ stack.wrap,
          StatusBadge(StatusBadge.label := "Running", StatusBadge.variant := StatusBadge.Variant.Running, StatusBadge.pulsing := true),
          StatusBadge(StatusBadge.label := "Queued",  StatusBadge.variant := StatusBadge.Variant.Queued),
          StatusBadge(StatusBadge.label := "Done",    StatusBadge.variant := StatusBadge.Variant.Success),
          StatusBadge(StatusBadge.label := "Failed",  StatusBadge.variant := StatusBadge.Variant.Warning)
        )
      )
    ),
    PageTemplate.behavior(
      "`pulsing := true` animates an opacity pulse via a JS setInterval (no @keyframes, no CSS animations)."
    ),
    PageTemplate.propsTable(
      ("label",   "String",                              "Status text."),
      ("variant", "Running|Queued|Success|Warning",      "State category."),
      ("pulsing", "Boolean",                             "Pulse the badge to indicate in-flight state.")
    )
  )

  def stat(): HtmlElement = PageTemplate(
    title = "Stat",
    summary = "Big-number summary tile with label, value, unit, hint, and an optional trend arrow."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Stat",
        """Stat(
          |  Stat.label := "ACTIVE RUNS",
          |  Stat.value := "12",
          |  Stat.unit := "/24",
          |  Stat.hint := "↑ 4 vs last week",
          |  Stat.trend := Stat.Trend.Up
          |)""".stripMargin
      )(
        SimpleGrid(columns = 3)(
          Stat(Stat.label := "ACTIVE RUNS", Stat.value := "12", Stat.unit := "/24", Stat.hint := "↑ 4 vs last week", Stat.trend := Stat.Trend.Up),
          Stat(Stat.label := "MEAN K", Stat.value := "8.4", Stat.unit := "", Stat.hint := "across 156 analyses"),
          Stat(Stat.label := "FAILURES", Stat.value := "3", Stat.unit := "%", Stat.hint := "↓ 1% vs last week", Stat.trend := Stat.Trend.Down)
        )
      )
    ),
    PageTemplate.propsTable(
      ("label", "String",         "Small uppercase label."),
      ("value", "String",         "Big number or value."),
      ("unit",  "String",         "Optional unit shown next to the value."),
      ("hint",  "String",         "Subtitle hint."),
      ("trend", "Up|Down|None",   "Optional trend arrow.")
    )
  )

  def timeline(): HtmlElement = PageTemplate(
    title = "Timeline",
    summary = "Vertical sequence of events with a bullet column and body."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Timeline",
        """Timeline(
          |  Timeline.items := Seq(
          |    Timeline.Item("Reference imported",  "2026-05-12 09:14", "Indexed 12,439 cells."),
          |    Timeline.Item("Pre-processing done", "2026-05-12 09:18", "Log-transform, scale by gene."),
          |    Timeline.Item("Compute complete",    "2026-05-12 09:43", "Reconstruction error 0.142.")
          |  )
          |)""".stripMargin
      )(
        Timeline(
          Timeline.items := Seq(
            Timeline.Item("Reference imported", "2026-05-12 09:14", "Indexed 12,439 cells across 28 samples."),
            Timeline.Item("Pre-processing done", "2026-05-12 09:18", "Log-transform, scale by gene, dropped low-variance genes."),
            Timeline.Item("Compute complete", "2026-05-12 09:43", "Reconstruction error 0.142."),
            Timeline.Item("Review", "2026-05-12 10:02", "Marked for follow-up.")
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("items", "Seq[Timeline.Item]", "Item(title, meta, body) entries, top-first.")
    )
  )

  def dataList(): HtmlElement = PageTemplate(
    title = "DataList",
    summary = "Key/value pairs panel. Horizontal (definition list) or vertical (label-over-value)."
  )(
    PageTemplate.section("Horizontal (default)")(
      PageTemplate.codedDemo(
        "DataList",
        """DataList(
          |  DataList.items := Seq(
          |    "Run ID"  -> "demo_run_2026",
          |    "K"       -> "8",
          |    "Status"  -> "Complete",
          |    "Owner"   -> "John Doe"
          |  )
          |)""".stripMargin
      )(
        DataList(
          DataList.items := Seq(
            "Run ID"   -> "demo_run_2026",
            "K"        -> "8",
            "Status"   -> "Complete",
            "Started"  -> "2 minutes ago",
            "Owner"    -> "John Doe"
          )
        )
      )
    ),
    PageTemplate.section("Vertical")(
      PageTemplate.codedDemo(
        "DataList.orientation := Vertical",
        """DataList(
          |  DataList.orientation := DataList.Orientation.Vertical,
          |  DataList.items := Seq(
          |    "REFERENCE"   -> "pbmc_10k_v3",
          |    "BARCODE SET" -> "10x-v3",
          |    "PIPELINE"    -> "cellranger 7.1"
          |  )
          |)""".stripMargin
      )(
        DataList(
          DataList.orientation := DataList.Orientation.Vertical,
          DataList.items := Seq(
            "REFERENCE"  -> "pbmc_10k_v3",
            "BARCODE SET" -> "10x-v3",
            "PIPELINE"   -> "cellranger 7.1"
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("items",       "Seq[(String,String)]", "(key, value) pairs."),
      ("orientation", "Horizontal|Vertical",  "Layout direction.")
    )
  )

  def table(): HtmlElement = PageTemplate(
    title = "Table",
    summary = "Themed data table for strings. For rich cell content, compose with custom rendering."
  )(
    PageTemplate.section("Striped")(
      PageTemplate.codedDemo(
        "Table",
        """Table(
          |  Table.striped := true,
          |  Table.columns := Seq("Run", "K", "Error", "Status"),
          |  Table.rows := Seq(
          |    Seq("demo_run_2026", "8",  "0.142", "Complete"),
          |    Seq("demo_run_2027", "10", "0.118", "Complete"),
          |    Seq("demo_run_2028", "8",  "—",    "Running")
          |  )
          |)""".stripMargin
      )(
        Table(
          Table.striped := true,
          Table.columns := Seq("Run", "K", "Error", "Status"),
          Table.rows := Seq(
            Seq("demo_run_2026",   "8",  "0.142", "Complete"),
            Seq("demo_run_2027",   "10", "0.118", "Complete"),
            Seq("demo_run_2028",   "8",  "—",     "Running"),
            Seq("demo_run_2029",   "12", "—",     "Queued")
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("columns", "Seq[String]",      "Column header labels."),
      ("rows",    "Seq[Seq[String]]", "Row data; one inner Seq per row."),
      ("striped", "Boolean",          "Alternate row background.")
    )
  )

  def clipboard(): HtmlElement = PageTemplate(
    title = "Clipboard",
    summary = "Copy-to-clipboard button. Briefly flips its label to ✓ Copied."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Clipboard",
        """Clipboard(
          |  Clipboard.value := "lui — typed Scala.js UI",
          |  Clipboard.label := "Copy ID"
          |)""".stripMargin
      )(
        Clipboard(Clipboard.value := "lui — typed Scala.js UI", Clipboard.label := "Copy ID")
      )
    ),
    PageTemplate.propsTable(
      ("value", "String", "Text to copy to the clipboard."),
      ("label", "String", "Button label. Default \"Copy\".")
    )
  )
}
