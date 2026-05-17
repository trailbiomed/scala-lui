package lui.e2e

class OverlaysSuite extends E2ESuite {

  // Each docs page renders the live demo AND a code-block string copy of the
  // same demo. Text locators therefore match twice — use `.first()` to scope to
  // the live demo, which always appears first in DOM order (PageTemplate.codedDemo).

  test("Modal opens on trigger click and closes on Cancel") {
    gotoSlug("modal")
    val openBtn = page.locator("button:has-text('Open dialog')")
    openBtn.waitFor()
    val title = page.locator("text=Confirm action").first()
    assert(!title.isVisible(), "expected Modal hidden initially")
    openBtn.click()
    title.waitFor()
    page.locator("button:has-text('Cancel')").click()
    page.waitForCondition(() => !title.isVisible())
  }

  test("Drawer opens on trigger click and shows the body") {
    gotoSlug("drawer")
    val openBtn = page.locator("button:has-text('Open drawer')")
    openBtn.waitFor()
    val title = page.locator("text=Run details").first()
    assert(!title.isVisible(), "expected Drawer hidden initially")
    openBtn.click()
    title.waitFor()
    page.locator("text=demo_run_2026").first().waitFor()
  }

  test("Popover toggle opens on click and closes on outside click") {
    gotoSlug("popover")
    val trigger = page.locator("button:has-text('Open popover')")
    trigger.waitFor()
    val body = page.locator("text=Quick actions").first()
    assert(!body.isVisible(), "expected Popover hidden initially")
    trigger.click()
    body.waitFor()
    page.locator("h1:has-text('Popover')").click()
    page.waitForCondition(() => !body.isVisible())
  }

  test("Menu opens on trigger click and emits select key on item click") {
    gotoSlug("menu")
    val trigger = page.locator("button[aria-label='Actions']")
    trigger.waitFor()
    trigger.click()
    val item = page.locator("text=Rename").first()
    item.waitFor()
    item.click()
    page.locator("text=selected: rename").waitFor()
  }

  test("Tooltip shows the label on hover (opacity transitions to 1)") {
    gotoSlug("tooltip")
    val play = page.locator("button[aria-label='Run']")
    play.waitFor()
    play.hover()
    // The bubble sibling has child.text bound to the label; opacity goes 0 → 1.
    page.waitForCondition { () =>
      val opacities = page
        .evaluate(
          """() => Array.from(document.querySelectorAll('span'))
            |  .filter(e => e.textContent === 'Top')
            |  .map(e => e.style.opacity)""".stripMargin
        )
        .asInstanceOf[java.util.List[_]]
      val sc = scala.jdk.CollectionConverters.ListHasAsScala(opacities).asScala
      sc.exists(_.toString == "1")
    }
  }

  test("HoverCard shows rich content on hover") {
    gotoSlug("hover-card")
    val trigger = page.locator("text=Hover me").first()
    trigger.waitFor()
    // HoverCard body is a <div> with inline opacity 0/1 (text always present).
    // Read the inline opacity of the body div containing "John Doe".
    def bodyOpacity(): String = page.evaluate(
      """() => {
        |  const divs = Array.from(document.querySelectorAll('div'));
        |  const body = divs.find(d =>
        |    d.textContent.includes('John Doe') &&
        |    d.style && d.style.opacity !== ''
        |  );
        |  return body ? body.style.opacity : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    assertEquals(bodyOpacity(), "0", "expected HoverCard body invisible initially")
    trigger.hover()
    page.waitForCondition(() => bodyOpacity() == "1")
  }

  test("ToggleTip opens on click and reveals the label") {
    gotoSlug("toggle-tip")
    val trigger = page.locator("button[aria-label='What is this?']")
    trigger.waitFor()
    val body = page.locator("text=Measured in wall-clock seconds across all workers.").first()
    assert(!body.isVisible(), "expected ToggleTip hidden initially")
    trigger.click()
    body.waitFor()
  }
}
