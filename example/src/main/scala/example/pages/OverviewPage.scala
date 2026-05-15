package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object OverviewPage {
  def apply(): HtmlElement = PageTemplate(
    title = "lui",
    summary = "Typed UI components for Laminar. Styles are set inline; the build is sbt-only."
  )(
    PageTemplate.section("What it is")(
      PageTemplate.paragraph(
        "lui is a catalog of UI components for Scala.js apps written with Laminar."
      ),
      PageTemplate.paragraph(
        "Styles are set inline via Scala-side declarations. There are no .css files, no <style> blocks, and no class names."
      )
    ),
    PageTemplate.section("How the docs are organized")(
      Listing()(
        Listing.item(span(typo.body, b("Foundations"), ": design tokens (colors, spacing, type) and the style primitives (", b("css.*"), ", ", b("stack.*"), ", ", b("typo.*"), ", ", b("themed()"), ") that components compose from.")),
        Listing.item(span(typo.body, b("Components"), ": each component has its own page with the use case, behavior notes, and live demos of variants and props.")),
        Listing.item(span(typo.body, b("Application primitives"), ": components shaped for a specific app domain. Kept for reference."))
      )
    ),
    PageTemplate.section("Theme")(
      PageTemplate.paragraph(
        "There's a theme toggle in the top-right of each page. Components use semantic theme tokens " +
          "(t.surface, t.brand, t.text, …), so the swap is instant."
      ),
      div(
        stack.row(spacing.md) ++ stack.wrap,
        Alert(Alert.title := "Info",    Alert.variant := Alert.Variant.Info,    Alert.body(span("Theme tokens flow through the components."))),
        Alert(Alert.title := "Success", Alert.variant := Alert.Variant.Success, Alert.body(span("Imported and indexed."))),
        Alert(Alert.title := "Warning", Alert.variant := Alert.Variant.Warning, Alert.body(span("Near quota limit."))),
        Alert(Alert.title := "Danger",  Alert.variant := Alert.Variant.Danger,  Alert.body(span("Build failed.")))
      )
    )
  )

  private def b(s: String): HtmlElement =
    span(themed(t => css.fontWeight(FontWeight.SemiBold) ++ css.color(t.text)), s)
}
