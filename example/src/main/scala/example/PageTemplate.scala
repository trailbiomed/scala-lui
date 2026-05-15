package example

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*
import lui.components.*

/** Layout helpers for a single component's documentation page. */
object PageTemplate {

  /** Themed prose style used by docs body text. Larger than `typo.body` for readability. */
  val prose: ThemedStyle = ThemedStyle(t =>
    css.fontSize(Length.px(15)) ++ css.color(t.text) ++ css.lineHeight(1.55)
  )

  /** Themed summary style — slightly larger muted text just under the title. */
  val proseMuted: ThemedStyle = ThemedStyle(t =>
    css.fontSize(Length.px(15)) ++ css.color(t.textMuted) ++ css.lineHeight(1.55)
  )

  /** Top-level page wrapper: title + body. */
  def apply(title: String, summary: String)(body: Modifier[HtmlElement]*): HtmlElement =
    div(
      stack.col(spacing.xxl) ++
        css.maxWidth(Length.px(960)) ++
        css.padding(spacing.xxl, spacing.xxl) ++
        css.raw("margin", "0 auto"),
      div(
        stack.col(spacing.sm),
        Heading(1)(title),
        p(proseMuted ++ css.margin(Length.px(0)) ++ css.maxWidth(Length.px(720)), summary)
      ),
      body
    )

  /** A labeled subsection inside a page. */
  def section(label: String)(body: Modifier[HtmlElement]*): HtmlElement =
    div(
      stack.col(spacing.lg),
      Heading(3)(label),
      body
    )

  /** A free-form paragraph of explanatory prose. */
  def paragraph(text: String): HtmlElement =
    p(prose ++ css.margin(Length.px(0)) ++ css.maxWidth(Length.px(720)), text)

  /** Live demo block with a code snippet rendered below it.
    *
    * The code is a hand-written string. It is not compiled against the live demo, so it can
    * drift if the surrounding API changes. Treat each snippet as illustrative. */
  def codedDemo(label: String, code: String)(demo: Modifier[HtmlElement]*): HtmlElement =
    div(
      stack.col(spacing.sm),
      if (label.nonEmpty) span(typo.eyebrow, label) else emptyNode,
      div(
        themed(t =>
          stack.col(spacing.lg) ++
            css.padding(spacing.lg) ++
            css.background(t.surfaceDim) ++
            css.borderRadius(radius.md)
        ),
        // Demo first
        div(demo),
        // Then the code, full width
        div(
          stack.col(spacing.xs) ++ css.width(Length.pct(100)),
          Code(Code.block := true, Code.text := code),
          span(typo.hint, "Illustrative. Not compiled against the demo.")
        )
      )
    )

  /** A live demo block with an optional caption. */
  def demo(caption: String = "")(body: Modifier[HtmlElement]*): HtmlElement =
    div(
      stack.col(spacing.md),
      if (caption.nonEmpty) span(typo.eyebrow, caption) else emptyNode,
      div(
        themed(t =>
          css.padding(spacing.xl) ++
            css.background(t.surface) ++
            css.border(Length.px(1), BorderStyle.Solid, t.border) ++
            css.borderRadius(radius.lg) ++
            stack.col(spacing.md)
        ),
        body
      )
    )

  /** Bullet list of behavior notes. */
  def behavior(items: String*): HtmlElement =
    div(
      stack.col(spacing.sm),
      Heading(3)("Behavior"),
      Listing()(items.map(t => Listing.item(prose, t))*)
    )

  /** A simple props/variants reference rendered as `Table`. */
  def propsTable(rows: (String, String, String)*): HtmlElement =
    div(
      stack.col(spacing.md),
      Heading(3)("Props"),
      Table(
        Table.columns := Seq("Prop", "Type", "Description"),
        Table.rows := rows.toSeq.map(r => Seq(r._1, r._2, r._3))
      )
    )

  /** Renders an empty Props section for components / helpers that take no props. */
  def noProps: HtmlElement =
    div(
      stack.col(spacing.md),
      Heading(3)("Props"),
      p(prose ++ css.margin(Length.px(0)), "No props.")
    )
}
