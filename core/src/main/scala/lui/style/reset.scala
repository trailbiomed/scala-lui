package lui.style

import org.scalajs.dom

/** One-shot global CSS bootstrap that lui apps should call once at
  * boot (typically the first line of `@main`).
  *
  * Why it exists:
  *
  *   - **`box-sizing: border-box`** on every element. Without this,
  *     a node with `width: 100%` plus horizontal padding overflows
  *     its parent by the padding amount, and any scrolling /
  *     `overflow: hidden` ancestor will clip the right edge
  *     (visible as rounded corners getting cut off, etc.). Every
  *     modern CSS framework — Chakra, Tailwind, etc. — installs
  *     this rule. lui's components don't add `box-sizing` per-token
  *     and rely on the reset.
  *
  *   - **Font smoothing**. macOS Retina + system-ui font renders
  *     notably heavier without `-webkit-font-smoothing: antialiased`
  *     than under Chakra's defaults (which apply the same two
  *     declarations). Without smoothing, lui-rendered text looks
  *     bolder than its Chakra peer at the same `font-weight`.
  *
  * Implementation: a single `<style>` element appended to `<head>`
  * for the box-sizing universal selector (there's no per-element
  * way to express `*` inline), plus inline body properties for the
  * smoothing.
  *
  * Idempotent — calling twice is harmless (the second call appends
  * a duplicate style element that has no observable effect). */
object reset {

  def install(): Unit = {
    val styleEl = dom.document.createElement("style")
    styleEl.textContent = "*, *::before, *::after { box-sizing: border-box; }"
    val _ = dom.document.head.appendChild(styleEl)

    dom.document.body.style.setProperty("-webkit-font-smoothing", "antialiased")
    dom.document.body.style.setProperty("-moz-osx-font-smoothing", "grayscale")
  }
}
