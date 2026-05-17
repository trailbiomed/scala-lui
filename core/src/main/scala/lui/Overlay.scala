package lui

import com.raquo.laminar.api.L.{Mod as _, *}
import org.scalajs.dom

/** Keyboard + focus plumbing for overlay components (Modal, Drawer, Popover, Menu).
  *
  *  - Escape closes the overlay (caller wires the close action).
  *  - On open: focus moves into the panel. The previously-focused element is
  *    remembered, and is restored on close.
  *  - While open: Tab and Shift+Tab cycle focus within the panel (focus trap)
  *    if `trapFocus` is true (default for modal-style overlays).
  *
  * Use [[install]] with `containerEl` set to the visible *panel* (the card / drawer body /
  * popover body — *not* the backdrop), the `openSignal` driving the overlay's
  * visibility, and `close` a Sink to invoke on Escape. */
object Overlay {

  /** Selectors for elements that can receive keyboard focus. */
  private val FocusableSelector: String =
    Seq(
      "a[href]",
      "area[href]",
      "input:not([disabled])",
      "select:not([disabled])",
      "textarea:not([disabled])",
      "button:not([disabled])",
      "iframe",
      "[tabindex]:not([tabindex='-1'])",
      "[contenteditable=true]"
    ).mkString(", ")

  /** Returns the in-DOM focusable descendants of `root`, filtered to those that
    * are visible (offsetParent != null). Order follows DOM order. */
  def focusables(root: dom.Element): Vector[dom.HTMLElement] = {
    val nodes = root.querySelectorAll(FocusableSelector)
    val out   = Vector.newBuilder[dom.HTMLElement]
    var i     = 0
    while (i < nodes.length) {
      nodes.item(i) match {
        case el: dom.HTMLElement =>
          // offsetParent is null when element is display:none — skip those.
          val visible = el.asInstanceOf[scala.scalajs.js.Dynamic].offsetParent != null
          if (visible) out += el
        case _ => ()
      }
      i += 1
    }
    out.result()
  }

  /** Returns the currently-focused element on the document, or null. */
  private def active(): dom.HTMLElement =
    dom.document.activeElement match {
      case el: dom.HTMLElement => el
      case _                   => null
    }

  /** Install Escape + focus-trap + focus-save/restore on `containerEl`, gated by
    * `openSignal`. `close` is invoked on Escape. */
  def install(
      containerEl: HtmlElement,
      openSignal: Signal[Boolean],
      close: () => Unit,
      trapFocus: Boolean = true,
      initialFocus: () => Option[dom.HTMLElement] = () => None
  ): Unit = {
    var lastFocused: dom.HTMLElement = null
    var isOpen: Boolean = false

    val openWriter: Observer[Boolean] = Observer[Boolean] { now =>
      val wasOpen = isOpen
      isOpen = now
      if (now && !wasOpen) {
        lastFocused = active()
        val _ = scala.scalajs.js.timers.setTimeout(0) {
          if (isOpen) {
            val first = initialFocus()
              .orElse(focusables(containerEl.ref).headOption)
              .orElse(Some(containerEl.ref.asInstanceOf[dom.HTMLElement]))
            first.foreach { el =>
              if (el eq containerEl.ref) {
                if (el.getAttribute("tabindex") == null)
                  el.setAttribute("tabindex", "-1")
              }
              try el.focus()
              catch { case _: Throwable => () }
            }
          }
        }
      } else if (!now && wasOpen) {
        if (lastFocused != null) {
          try lastFocused.focus()
          catch { case _: Throwable => () }
          lastFocused = null
        }
      }
    }
    val keyHandler: Observer[dom.KeyboardEvent] = Observer[dom.KeyboardEvent] { ev =>
      if (isOpen) {
        if (ev.key == "Escape") {
          ev.stopPropagation()
          ev.preventDefault()
          close()
        } else if (trapFocus && ev.key == "Tab") {
          val fs = focusables(containerEl.ref)
          if (fs.isEmpty) {
            ev.preventDefault()
          } else {
            val first = fs.head
            val last  = fs.last
            val cur   = active()
            if (ev.shiftKey && (cur == first || cur == containerEl.ref)) {
              ev.preventDefault()
              try last.focus() catch { case _: Throwable => () }
            } else if (!ev.shiftKey && cur == last) {
              ev.preventDefault()
              try first.focus() catch { case _: Throwable => () }
            }
          }
        }
      }
    }
    containerEl.amend(
      openSignal --> openWriter,
      onKeyDown --> keyHandler
    )
  }
}
