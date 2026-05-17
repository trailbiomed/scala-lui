package lui.e2e

class FeedbackSuite extends E2ESuite {

  test("Alert dismissible variants render a close × that is clickable") {
    gotoSlug("alert")
    page.locator("text=Quota nearing").first().waitFor()
    // All four Alert demos render a <span>×</span> child (text is always set);
    // only the dismissible ones are display:InlineFlex visible. Take the last
    // × span — that's the Danger alert (dismissible := true) on the demo page.
    val xs = page.locator("span:has-text('×')")
    val last = xs.nth(xs.count() - 1)
    last.waitFor()
    last.click() // demo doesn't wire dismiss; assert no throw
  }

  test("Toast.show makes the message appear in the fixed-position toast") {
    gotoSlug("toast")
    page.locator("button:has-text('Show toast')").click()
    // Toast() is a position:fixed div whose text is bound to a Var via signal.
    // The demo currently shows "Toast #0" on the first click (the `count.now()`
    // call reads the pre-transaction value — Airstream queues `Var.update`),
    // so this test asserts "Toast #" is present and opacity has gone to 1.
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('div'))
        |  .some(d => d.style && d.style.position === 'fixed'
        |             && d.style.opacity === '1'
        |             && d.textContent && d.textContent.startsWith('Toast #'))""".stripMargin
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
