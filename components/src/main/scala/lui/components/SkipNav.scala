package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*
import org.scalajs.dom

/** Keyboard-only "skip to main content" link. Hidden until focused.
  *
  * Activating it moves focus to the target and scrolls there in script rather than letting
  * the browser follow the fragment — the same shape as `Link.scrollTarget`. Following the
  * fragment would write `#main` into `location.hash`, which an app that routes on the hash
  * reads as a route it does not have, landing the reader on a "no such view" page; and even
  * with a tolerant router the URL would stop describing the view. The `href` is still set
  * so the keyboard and the status bar see a real target.
  *
  * Moving focus is the point, not scrolling: a fragment jump to a non-focusable region
  * scrolls the page but leaves focus in the navbar, so the next Tab goes straight back into
  * the nav and the link appears to do nothing. That needs the target to be focusable —
  * give it `SkipNav.target(id)`, which sets both the id and the `tabindex="-1"` that makes
  * a region programmatically focusable without adding it to the Tab order. */
object SkipNav {

  /** Mark the region a `SkipNav` points at: sets its `id` and makes it focusable. */
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
