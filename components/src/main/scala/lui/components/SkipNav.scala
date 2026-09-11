package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*
import org.scalajs.dom

/** Keyboard-only "skip to main content" link. Hidden until focused.
  *
  * Moves focus to the target in script rather than letting the browser follow the
  * fragment, which a hash-routed app would read as a route it does not have. Mark the
  * destination with [[SkipNav.target]]: a fragment jump to a region that isn't focusable
  * scrolls the page but leaves focus behind, so the next Tab returns to the nav and the
  * link appears to do nothing. */
object SkipNav {

  /** Marks the region a [[SkipNav]] points at, setting its `id` and making it focusable
    * without adding it to the Tab order. */
  def target(id: String): Modifier[HtmlElement] =
    new Modifier[HtmlElement] {
      override def apply(el: HtmlElement): Unit = el.amend(idAttr := id, tabIndex := -1)
    }

  def apply(targetId: String, label: String = "Skip to main content"): HtmlElement = {
    val focused = Var(false)
    a(
      href := s"#$targetId",
      focused.signal.styled { (t, f) =>
        if (f)
          css.position("fixed") ++
            css.raw("top", spacing.md.toCss) ++
            css.raw("left", spacing.md.toCss) ++
            css.padding(spacing.sm, spacing.lg) ++
            css.background(t.brand) ++
            css.color(t.onBrand) ++
            css.borderRadius(radius.md) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.zIndex(100) ++
            css.raw("text-decoration", "none")
        else
          css.position("absolute") ++
            css.width(Length.px(1)) ++ css.height(Length.px(1)) ++
            css.padding(Length.zero) ++
            css.overflow("hidden") ++
            css.raw("clip", "rect(0,0,0,0)") ++
            css.raw("white-space", "nowrap") ++
            css.raw("border", "0")
      },
      onFocus.mapTo(true) --> focused.writer,
      onBlur.mapTo(false) --> focused.writer,
      onClick --> Observer[dom.MouseEvent] { ev =>
        ev.preventDefault()
        dom.document.getElementById(targetId) match {
          case region: dom.HTMLElement =>
            region.focus()
            region.scrollIntoView()
          case _ => ()
        }
      },
      label
    )
  }
}
