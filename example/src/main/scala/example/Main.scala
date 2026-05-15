package example

import com.raquo.laminar.api.L.{Mod as _, *}
import org.scalajs.dom
import lui.*
import lui.components.{Button, Toast}
import lui.style.*
import example.pages.CoffeeMachinePage

/** Slug that takes over the whole content area (no sidebar). */
private val CoffeeSlug = "coffee-machine"

@main def main(): Unit = {
  reset.install()
  val mount = dom.document.getElementById("app")
  if (mount != null) {
    Theme.signal.foreach { t =>
      dom.document.body.style.backgroundColor = t.bg.toCss
      dom.document.body.style.color = t.text.toCss
    }(unsafeWindowOwner)

    val initial =
      Option(dom.window.location.hash).map(_.stripPrefix("#")).filter(_.nonEmpty)
        .getOrElse(DocsIndex.firstSlug)
    val slug: Var[String] = Var(initial)

    // Sync slug -> URL hash and reflect back-button changes.
    slug.signal.foreach(s => dom.window.location.hash = s)(unsafeWindowOwner)
    dom.window.addEventListener(
      "hashchange",
      (_: dom.Event) => {
        val s = dom.window.location.hash.stripPrefix("#")
        if (s.nonEmpty && s != slug.now()) slug.set(s)
      }
    )

    val _ = render(mount, App(slug))
  }
}

private def App(slug: Var[String]): HtmlElement = {
  div(
    css.raw("min-height", "100vh"),
    Header(slug),
    child <-- slug.signal.map { s =>
      if (s == CoffeeSlug) coffeeView()
      else docsView(slug)
    },
    Toast()
  )
}

/** Docs layout: sidebar on the left, page content on the right. */
private def docsView(slug: Var[String]): HtmlElement =
  div(
    stack.row() ++ css.alignItems("flex-start"),
    Sidebar(slug),
    div(
      css.raw("flex", "1 1 0") ++ css.raw("min-width", "0"),
      child <-- slug.signal.map { s =>
        DocsIndex.all.get(s) match {
          case Some(e) => e.render()
          case None    => DocsIndex.all(DocsIndex.firstSlug).render()
        }
      }
    )
  )

/** Coffee demo: standalone, full-width, no sidebar. */
private def coffeeView(): HtmlElement =
  div(
    css.raw("flex", "1 1 0") ++ css.raw("min-width", "0"),
    CoffeeMachinePage()
  )

private def Header(slug: Var[String]): HtmlElement =
  div(
    themed(t =>
      stack.between(spacing.md) ++
        css.padding(spacing.lg, spacing.xxl) ++
        css.raw("border-bottom", s"1px solid ${t.border.toCss}") ++
        css.background(t.surface) ++
        css.position("sticky") ++
        css.raw("top", "0") ++
        css.zIndex(10)
    ),
    div(
      stack.row(spacing.md),
      span(themed(t => css.fontSize(fontSizes.display) ++ css.fontWeight(FontWeight.SemiBold) ++ css.color(t.brand)), "lui"),
      span(themed(t => css.fontSize(fontSizes.lg) ++ css.color(t.textMuted)), "/ docs")
    ),
    div(
      stack.row(spacing.md) ++ css.alignItems("center"),
      headerLink("Docs",   slug, DocsIndex.firstSlug, isDocs = true),
      headerLink("Coffee demo", slug, CoffeeSlug, isDocs = false),
      Button(
        Button.label <-- Theme.signal.map(t => if (t.isDark) "☀ light" else "☾ dark"),
        Button.variant := Button.Variant.Ghost,
        Button.size := Button.Size.Small,
        Button.click.foreach(_ => Theme.toggle())
      )
    )
  )

/** Header nav link. `isDocs = true` matches when the slug points at any docs page (not coffee). */
private def headerLink(label: String, slug: Var[String], target: String, isDocs: Boolean): Modifier[HtmlElement] = {
  val activeS = slug.signal.map(s => if (isDocs) s != CoffeeSlug else s == CoffeeSlug)
  Button(
    Button.label := label,
    Button.size := Button.Size.Small,
    Button.variant <-- activeS.map(a => if (a) Button.Variant.Secondary else Button.Variant.Ghost),
    Button.click.mapTo(target) --> slug.writer
  )
}

private def Sidebar(slug: Var[String]): HtmlElement =
  div(
    themed(t =>
      stack.col(spacing.xl) ++
        css.width(Length.px(240)) ++
        css.raw("flex", "0 0 auto") ++
        css.padding(spacing.xl, spacing.lg) ++
        css.raw("border-right", s"1px solid ${t.border.toCss}") ++
        css.raw("position", "sticky") ++
        css.raw("top", "57px") ++
        css.raw("max-height", "calc(100vh - 57px)") ++
        css.raw("overflow-y", "auto") ++
        css.raw("box-sizing", "border-box")
    ),
    DocsIndex.categories.map { cat =>
      div(
        stack.col(spacing.xs),
        span(typo.eyebrow, cat.label),
        div(
          stack.col(Length.px(2)),
          cat.entries.map(e => navItem(e, slug))
        )
      )
    }
  )

private def navItem(entry: DocEntry, slug: Var[String]): HtmlElement = {
  val hovered = Var(false)
  div(
    Signal.combine(slug.signal, hovered.signal).styled { case (t, (s, h)) =>
      val selected = s == entry.slug
      val (bg, fg) =
        if (selected) (t.brandSoft, t.brand)
        else if (h) (t.surfaceDim, t.text)
        else (Color.transparent, t.textMuted)
      css.padding(Length.px(4), spacing.md) ++
        css.borderRadius(radius.sm) ++
        css.background(bg) ++
        css.color(fg) ++
        css.cursor("pointer") ++
        css.fontSize(fontSizes.lg) ++
        css.fontWeight(if (s == entry.slug) FontWeight.Medium else FontWeight.Regular)
    },
    onMouseEnter.mapTo(true) --> hovered.writer,
    onMouseLeave.mapTo(false) --> hovered.writer,
    onClick.mapTo(entry.slug) --> slug.writer,
    entry.label
  )
}
