package lui.e2e

class ExtraButtonsSuite extends E2ESuite {

  test("IconButton renders an aria-labeled button with the icon glyph") {
    gotoSlug("icon-button")
    val play = page.locator("button[aria-label='Play']")
    play.waitFor()
    assert(play.textContent().contains("▶"))
    val edit = page.locator("button[aria-label='Edit']")
    edit.waitFor()
    assert(edit.textContent().contains("✎"))
  }

  test("CloseButton renders a × button that is clickable") {
    gotoSlug("close-button")
    // Two close buttons (Default + Small). Both have × as their content.
    val btns = page.locator("button:has-text('×')")
    page.waitForCondition(() => btns.count() >= 2)
    btns.first().click() // does not throw
  }

  test("DownloadTrigger emits an <a download> with href + filename") {
    gotoSlug("download-trigger")
    val link = page.locator("a:has-text('Download report')")
    link.waitFor()
    assertEquals(link.getAttribute("download"), "report.txt")
    assertEquals(link.getAttribute("href"), "data:text/plain,Example")
  }
}
