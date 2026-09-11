package lui.e2e

class FeedbackSuite extends E2ESuite {

  test("Alert dismissible variants render a close × that is clickable") {
    gotoSlug("alert")
    page.locator("text=Quota nearing").first().waitFor()
    // Every Alert renders a <span>×</span>; only the dismissible ones make it visible,
    // so scope to those rather than to position on the page.
    val xs = page.locator("span:has-text('×'):visible")
    val last = xs.nth(xs.count() - 1)
    last.waitFor()
    last.click() // demo doesn't wire dismiss; assert no throw
  }

  test("Toast.show makes the message appear in an aria-live region") {
    gotoSlug("toast")
    page.locator("button:has-text('Show toast')").click()
    // Each toast is a role=status (or role=alert for errors) row with aria-live.
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('[aria-live]'))
        |  .some(d => (d.textContent || '').startsWith('Toast #'))""".stripMargin
    )
  }

  test("ProgressBar page mounts with the headings") {
    gotoSlug("progress-bar")
    // :has-text is substring, so 'Determinate' matches both h3s.
    // Use Playwright's :text-is for exact match.
    page.locator("h3:text-is('Determinate')").waitFor()
    page.locator("h3:text-is('Indeterminate')").waitFor()
  }

  test("ProgressCircle shows percentage labels when showLabel is true") {
    gotoSlug("progress-circle")
    page.locator("text=42%").first().waitFor()
    page.locator("text=72%").first().waitFor()
    page.locator("text=15%").first().waitFor()
  }

  test("Spinner page renders the size demo") {
    gotoSlug("spinner")
    // Spinner is a styled <div>, not an SVG. Just verify the page is mounted.
    page.locator("h1:text-is('Spinner')").waitFor()
    page.locator("h3:text-is('Sizes')").waitFor()
  }

  test("Skeleton page renders") {
    gotoSlug("skeleton")
    page.locator("h1:text-is('Skeleton')").waitFor()
  }

  test("EmptyState page renders icon, title, description, and an action button") {
    gotoSlug("empty-state")
    page.locator("text=∅").first().waitFor()
    page.locator("text=No projects yet").first().waitFor()
    page.locator("button:has-text('+ New project')").waitFor()
  }
}
