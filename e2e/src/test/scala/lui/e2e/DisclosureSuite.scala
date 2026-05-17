package lui.e2e

class DisclosureSuite extends E2ESuite {

  test("Collapsible Show button toggles body visibility") {
    gotoSlug("collapsible")
    val toggle = page.locator("button:has-text('Show')")
    toggle.waitFor()
    val body = page.locator("text=Hidden details revealed.").first()
    assert(!body.isVisible(), "expected Collapsible body hidden initially")
    toggle.click()
    body.waitFor()
    page.locator("button:has-text('Hide')").waitFor()
    page.locator("button:has-text('Hide')").click()
    page.waitForCondition(() => !body.isVisible())
  }

  test("Pagination clicking a sibling page updates the bound page") {
    gotoSlug("pagination")
    def onPageSpanText(): String = page.evaluate(
      """() => {
        |  const spans = Array.from(document.querySelectorAll('span'));
        |  const hit = spans.find(s => s.textContent && s.textContent.startsWith('on page '));
        |  return hit ? hit.textContent : '';
        |}""".stripMargin
    ).asInstanceOf[String]

    // `page.waitForCondition` + `evaluate` stalled on this slug for reasons
    // we couldn't pin down; an explicit Scala-side loop polls reliably.
    def awaitPage(target: Int): Unit = {
      val deadline = System.currentTimeMillis() + 5000
      var current = ""
      while (System.currentTimeMillis() < deadline && current != s"on page $target") {
        current = onPageSpanText()
        if (current != s"on page $target") Thread.sleep(50)
      }
      if (current != s"on page $target")
        fail(s"never saw 'on page $target' — last seen: '$current'")
    }

    awaitPage(3)
    // `:has-text` is substring — 'button:has-text("2")' also matches "12".
    // Use `:text-is` for an exact match.
    page.locator("button:text-is('4')").click()
    awaitPage(4)
    page.locator("button:text-is('5')").click()
    awaitPage(5)
    page.locator("button:text-is('1')").click()
    awaitPage(1)
  }

  test("Steps Next/Back buttons advance and rewind the stepper") {
    gotoSlug("steps")
    // No bound-state display in the demo. Verify that the four step labels
    // are present, and that Next/Back clicks don't throw.
    page.locator("text=Reference").first().waitFor()
    page.locator("text=Pre-process").first().waitFor()
    page.locator("text=Compute K").first().waitFor()
    page.locator("text=Review").first().waitFor()
    page.locator("button:has-text('Next →')").click()
    page.locator("button:has-text('Next →')").click()
    page.locator("button:has-text('← Back')").click()
  }

  test("Breadcrumb click emits the crumb key") {
    gotoSlug("breadcrumb")
    page.locator("text=Workbench").first().click()
    page.locator("text=last clicked: wb").waitFor()
    page.locator("text=References").first().click()
    page.locator("text=last clicked: ref").waitFor()
  }
}
