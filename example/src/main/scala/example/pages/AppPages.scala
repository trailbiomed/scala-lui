package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object AppPages {

  def pageHeader(): HtmlElement = PageTemplate(
    title = "PageHeader",
    summary = "Top-of-page bar with title, an optional back link, and a right-aligned actions slot."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "PageHeader",
        """PageHeader(
          |  PageHeader.title := "Workbench",
          |  PageHeader.back := "← Home",
          |  PageHeader.onBack.foreach(_ => goHome()),
          |  PageHeader.right(
          |    Button(Button.label := "New project",
          |      Button.variant := Button.Variant.Primary,
          |      Button.size := Button.Size.Small),
          |    Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Sm)
          |  )
          |)""".stripMargin
      )(
        PageHeader(
          PageHeader.title := "Workbench",
          PageHeader.back := "← Home",
          PageHeader.right(
            Button(Button.label := "New project", Button.variant := Button.Variant.Primary, Button.size := Button.Size.Small),
            Avatar(Avatar.name := "John Doe", Avatar.size := Avatar.Size.Sm)
          )
        )
      )
    ),
    PageTemplate.propsTable(
      ("title",  "String",     "Page title."),
      ("back",   "String",     "Optional back-link label. Empty string hides it."),
      ("onBack", "Out[Unit]",  "Back link click event."),
      ("right",  "Slot",       "Right-side actions.")
    )
  )

  def sectionLabel(): HtmlElement = PageTemplate(
    title = "SectionLabel",
    summary = "Small uppercase heading for sections inside a panel."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "SectionLabel",
        """SectionLabel(SectionLabel.text := "Pre-processing")
          |span(typo.muted, "Content for the section goes here.")""".stripMargin
      )(
        div(stack.col(spacing.sm),
          SectionLabel(SectionLabel.text := "Pre-processing"),
          span(typo.muted, "Content for the section goes here.")
        )
      )
    ),
    PageTemplate.propsTable(
      ("text", "String", "Label text. Rendered in uppercase via CSS.")
    )
  )

  def metricCell(): HtmlElement = PageTemplate(
    title = "MetricCell",
    summary = "Cell in a K-grid showing a candidate K value, its quality score, and a sparkbar."
  )(
    PageTemplate.section("States")(
      PageTemplate.codedDemo(
        "MetricCell",
        """MetricCell(
          |  MetricCell.value := "K=8",
          |  MetricCell.score := "0.92",
          |  MetricCell.bar   := Some(0.92),
          |  MetricCell.state := MetricCell.State.Active
          |)""".stripMargin
      )(
        div(stack.row(spacing.lg) ++ stack.wrap,
          MetricCell(MetricCell.value := "K=4",  MetricCell.score := "0.84", MetricCell.bar := Some(0.84), MetricCell.state := MetricCell.State.Idle),
          MetricCell(MetricCell.value := "K=8",  MetricCell.score := "0.92", MetricCell.bar := Some(0.92), MetricCell.state := MetricCell.State.Active),
          MetricCell(MetricCell.value := "K=10", MetricCell.score := "—",     MetricCell.bar := None,        MetricCell.state := MetricCell.State.Running),
          MetricCell(MetricCell.value := "K=12", MetricCell.score := "—",     MetricCell.bar := None,        MetricCell.state := MetricCell.State.Queued)
        )
      )
    ),
    PageTemplate.propsTable(
      ("value", "String",          "Big value label (e.g. \"K=8\")."),
      ("score", "String",          "Quality score under the value."),
      ("bar",   "Option[Double]",  "Optional sparkbar fraction between 0 and 1."),
      ("state", "Idle|Active|Running|Queued", "Visual state."),
      ("click", "Out[Unit]",       "Click event.")
    )
  )

  def referenceCard(): HtmlElement = PageTemplate(
    title = "ReferenceCard",
    summary = "Card describing a reference dataset (source, sample count, organism)."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "ReferenceCard",
        """ReferenceCard(
          |  ReferenceCard.name := "PBMC 10k v3",
          |  ReferenceCard.icon := "🧬",
          |  ReferenceCard.sourceLabel := "10x Genomics",
          |  ReferenceCard.sampleCount := 12,
          |  ReferenceCard.organism := "Homo sapiens",
          |  ReferenceCard.description := "PBMCs from 8 healthy donors.",
          |  ReferenceCard.lastUsed := "2026-05-12"
          |)""".stripMargin
      )(
        ReferenceCard(
          ReferenceCard.name := "PBMC 10k v3",
          ReferenceCard.icon := "🧬",
          ReferenceCard.sourceLabel := "10x Genomics",
          ReferenceCard.sampleCount := 12,
          ReferenceCard.organism := "Homo sapiens",
          ReferenceCard.description := "Peripheral blood mononuclear cells from 8 healthy donors.",
          ReferenceCard.lastUsed := "2026-05-12"
        )
      )
    ),
    PageTemplate.propsTable(
      ("name",        "String",    "Reference name."),
      ("icon",        "String",    "Glyph shown next to the name."),
      ("sourceLabel", "String",    "Where the data came from."),
      ("sampleCount", "Int",       "Number of samples."),
      ("organism",    "String",    "Species / organism."),
      ("description", "String",    "Free-form description."),
      ("lastUsed",    "String",    "Last-used date label."),
      ("click",       "Out[Unit]", "Click event.")
    )
  )
}
