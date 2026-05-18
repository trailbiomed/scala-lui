package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object AppPages {

  def navbar(): HtmlElement = PageTemplate(
    title = "Navbar",
    summary = "Application navigation bar with start, center, and end slots. Supports solid/subtle/transparent variants, three sizes, optional border, and sticky positioning."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Navbar",
        """Navbar(
          |  Navbar.start(
          |    span(typo.h2, "lui"),
          |    span(typo.muted, "/ console")
          |  ),
          |  Navbar.center(
          |    Button(Button.label := "Docs",    Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small),
          |    Button(Button.label := "Recipes", Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small),
          |    Button(Button.label := "Blog",    Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small)
          |  ),
          |  Navbar.end(
          |    Button(Button.label := "Sign in", Button.variant := Button.Variant.Primary, Button.size := Button.Size.Small),
          |    Avatar(Avatar.name := "Ada Lovelace", Avatar.size := Avatar.Size.Sm)
          |  )
          |)""".stripMargin
      )(
        Navbar(
          Navbar.sticky := false,
          Navbar.start(
            span(typo.h2 ++ css.margin(Length.px(0)), "lui"),
            span(typo.muted, "/ console")
          ),
          Navbar.center(
            Button(Button.label := "Docs",    Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small),
            Button(Button.label := "Recipes", Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small),
            Button(Button.label := "Blog",    Button.variant := Button.Variant.Ghost, Button.size := Button.Size.Small)
          ),
          Navbar.end(
            Button(Button.label := "Sign in", Button.variant := Button.Variant.Primary, Button.size := Button.Size.Small),
            Avatar(Avatar.name := "Ada Lovelace", Avatar.size := Avatar.Size.Sm)
          )
        )
      )
    ),
    PageTemplate.section("Variants")(
      PageTemplate.codedDemo(
        "Solid (default)",
        """Navbar(Navbar.variant := Navbar.Variant.Solid, …)"""
      )(
        Navbar(
          Navbar.sticky := false,
          Navbar.variant := Navbar.Variant.Solid,
          Navbar.start(span(typo.h2 ++ css.margin(Length.px(0)), "Solid")),
          Navbar.end(Button(Button.label := "Action", Button.size := Button.Size.Small))
        )
      ),
      PageTemplate.codedDemo(
        "Subtle",
        """Navbar(Navbar.variant := Navbar.Variant.Subtle, …)"""
      )(
        Navbar(
          Navbar.sticky := false,
          Navbar.variant := Navbar.Variant.Subtle,
          Navbar.start(span(typo.h2 ++ css.margin(Length.px(0)), "Subtle")),
          Navbar.end(Button(Button.label := "Action", Button.size := Button.Size.Small))
        )
      ),
      PageTemplate.codedDemo(
        "Transparent",
        """Navbar(Navbar.variant := Navbar.Variant.Transparent, Navbar.bordered := false, …)"""
      )(
        Navbar(
          Navbar.sticky := false,
          Navbar.variant := Navbar.Variant.Transparent,
          Navbar.bordered := false,
          Navbar.start(span(typo.h2 ++ css.margin(Length.px(0)), "Transparent")),
          Navbar.end(Button(Button.label := "Action", Button.size := Button.Size.Small))
        )
      )
    ),
    PageTemplate.section("Sizes")(
      PageTemplate.codedDemo(
        "Sm / Md / Lg",
        """Navbar(Navbar.size := Navbar.Size.Sm, …)
          |Navbar(Navbar.size := Navbar.Size.Md, …)
          |Navbar(Navbar.size := Navbar.Size.Lg, …)""".stripMargin
      )(
        div(stack.col(spacing.md),
          Navbar(
            Navbar.sticky := false,
            Navbar.size := Navbar.Size.Sm,
            Navbar.start(span(typo.label, "Sm — 48px"))
          ),
          Navbar(
            Navbar.sticky := false,
            Navbar.size := Navbar.Size.Md,
            Navbar.start(span(typo.label, "Md — 56px"))
          ),
          Navbar(
            Navbar.sticky := false,
            Navbar.size := Navbar.Size.Lg,
            Navbar.start(span(typo.label, "Lg — 64px"))
          )
        )
      )
    ),
    PageTemplate.section("Minimal — brand + actions only")(
      PageTemplate.codedDemo(
        "Navbar (no center)",
        """Navbar(
          |  Navbar.start(span(typo.h2, "lui")),
          |  Navbar.end(ThemePicker())
          |)""".stripMargin
      )(
        Navbar(
          Navbar.sticky := false,
          Navbar.start(span(typo.h2 ++ css.margin(Length.px(0)), "lui")),
          Navbar.end(ThemePicker())
        )
      )
    ),
    PageTemplate.propsTable(
      ("sticky",   "Boolean",                       "Pin to top of viewport (position: sticky). Defaults to true."),
      ("bordered", "Boolean",                       "Show a bottom border. Defaults to true."),
      ("variant",  "Solid | Subtle | Transparent",  "Background treatment. Solid uses surface + shadow, Subtle uses surfaceDim, Transparent is transparent."),
      ("size",     "Sm | Md | Lg",                  "Bar height (48 / 56 / 64 px)."),
      ("start",    "Slot",                          "Left content. Use for brand / logo."),
      ("center",   "Slot",                          "Center content. Use for primary navigation."),
      ("end",      "Slot",                          "Right content. Use for actions and account.")
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
