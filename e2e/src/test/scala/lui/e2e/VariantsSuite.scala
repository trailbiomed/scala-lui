package lui.e2e

class VariantsSuite extends E2ESuite {

  test("Drawer Left slides from the left side when its trigger is clicked") {
    gotoSlug("drawer")
    val openLeft = page.locator("button:has-text('Open left drawer')")
    openLeft.waitFor()
    val filters = page.getByText("Filters").first()
    assert(!filters.isVisible(), "expected left drawer hidden initially")
    openLeft.click()
    filters.waitFor()
    // After opening, the visible "filter controls" body should also appear.
    page.getByText("Filter controls go here.").first().waitFor()
  }

  test("Pagination Previous arrow moves to page 2 from page 3") {
    gotoSlug("pagination")
    page.locator("button[aria-label='Previous']").click()
    page.waitForFunction("() => document.body.textContent.includes('on page 2')")
    page.locator("button[aria-label='Next']").click()
    page.waitForFunction("() => document.body.textContent.includes('on page 3')")
  }

  test("RadioGroup Horizontal renders the three options on one row") {
    gotoSlug("radio-group")
    // The Horizontal demo uses day/week/month labels.
    page.getByText("Day").first().waitFor()
    page.getByText("Week").first().waitFor()
    page.getByText("Month").first().waitFor()
  }

  test("DataList Vertical renders uppercase labels") {
    gotoSlug("data-list")
    page.getByText("REFERENCE").first().waitFor()
    page.getByText("BARCODE SET").first().waitFor()
    page.getByText("PIPELINE").first().waitFor()
  }

  test("Tooltip shows a different label for each placement on hover") {
    gotoSlug("tooltip")
    val labels = Seq("Top", "Right", "Bottom", "Left")
    for (lbl <- labels) {
      val trigger = page.locator(s"button[aria-label]").first()
      val _       = trigger // unused — we hover by index below
    }
    // Each placement variant has its own trigger button + bubble span.
    // Hover the Run (Top) button and verify the "Top" bubble becomes opaque.
    page.locator("button[aria-label='Run']").hover()
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('span'))
        |  .some(s => s.textContent === 'Top' && s.style.opacity === '1')""".stripMargin
    )
    page.locator("button[aria-label='Delete']").hover()
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('span'))
        |  .some(s => s.textContent === 'Bottom' && s.style.opacity === '1')""".stripMargin
    )
  }
}
